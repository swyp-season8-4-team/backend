package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.dto.login.LoginRequest;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.logout.LogoutResponse;
import org.swyp.dessertbee.auth.dto.signup.SignUpRequest;
import org.swyp.dessertbee.auth.service.AuthService;
import jakarta.validation.Valid;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;


@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "회원가입 (completed)",
            description = "새로운 사용자를 등록합니다. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.",
            parameters = {
                    @Parameter(
                            name = "X-Device-ID",
                            description = "디바이스 식별자 (앱 환경에서 사용). 없을 경우 서버에서 생성됩니다.",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    ),
                    @Parameter(
                            name = "deviceId",
                            description = "디바이스 식별자 쿠키 (웹 환경에서 사용). Nginx에서 X-Device-ID 헤더로 변환됩니다.",
                            in = ParameterIn.COOKIE,
                            schema = @Schema(type = "string"),
                            required = false
                    )
            }
    )
    @ApiResponse( responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)) )
    @ApiErrorResponses({ErrorCode.PASSWORD_MISMATCH, ErrorCode.DUPLICATE_NICKNAME, ErrorCode.DUPLICATE_EMAIL})
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @RequestHeader("X-Email-Verification-Token") String verificationToken,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @Valid @RequestBody SignUpRequest request
    ) {
        log.debug("회원가입 요청: {}", request.getEmail());
        LoginResponse signupResponse = authService.signup(request, verificationToken, deviceId);
        return ResponseEntity.ok(signupResponse);
    }


    @Operation(
            summary = "로그인 (completed)",
            description = "이메일과 비밀번호로 로그인합니다. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.",
            parameters = {
                    @Parameter(
                            name = "X-Device-ID",
                            description = "디바이스 식별자 (앱 환경에서 사용). 없을 경우 서버에서 생성됩니다.",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    ),
                    @Parameter(
                            name = "deviceId",
                            description = "디바이스 식별자 쿠키 (웹 환경에서 사용). Nginx에서 X-Device-ID 헤더로 변환됩니다.",
                            in = ParameterIn.COOKIE,
                            schema = @Schema(type = "string"),
                            required = false
                    )
            }
    )
    @ApiResponse( responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)) )
    @ApiErrorResponses({ErrorCode.PASSWORD_MISMATCH, ErrorCode.USER_NOT_FOUND})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {
        LoginResponse loginResponse = authService.login(request, deviceId);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(
            summary = "로그아웃 (completed)",
            description = "현재 로그인된 사용자를 로그아웃합니다. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 해당 디바이스의 세션만 종료합니다. 하지만 X-Device-ID 헤더를 전달받지 못한 경우에는 해당 유저가 가지고 있는 모든 리프레시 토큰을 무효화합니다.",
            parameters = {
                    @Parameter(
                            name = "X-Device-ID",
                            description = "디바이스 식별자 (앱 환경에서 사용)",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    ),
                    @Parameter(
                            name = "deviceId",
                            description = "디바이스 식별자 쿠키 (웹 환경에서 사용). Nginx에서 X-Device-ID 헤더로 변환됩니다.",
                            in = ParameterIn.COOKIE,
                            schema = @Schema(type = "string"),
                            required = false
                    )
            }
    )
    @ApiResponse( responseCode = "200", description = "로그아웃 성공" )
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {
        String accessToken = authHeader.substring(7);
        LogoutResponse logoutResponse = authService.logout(accessToken, deviceId);
        return ResponseEntity.ok(logoutResponse);
    }

    @Operation(
            summary = "토큰 재발급 (completed)",
            description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.",
            parameters = {
                    @Parameter(
                            name = "X-Device-ID",
                            description = "디바이스 식별자 (앱 환경에서 사용)",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    ),
                    @Parameter(
                            name = "deviceId",
                            description = "디바이스 식별자 쿠키 (웹 환경에서 사용). Nginx에서 X-Device-ID 헤더로 변환됩니다.",
                            in = ParameterIn.COOKIE,
                            schema = @Schema(type = "string"),
                            required = true
                    )
            }
    )
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공", content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_VERIFICATION_TOKEN, ErrorCode.EXPIRED_VERIFICATION_TOKEN, ErrorCode.DEVICE_ID_MISSING})
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "리프레시 토큰 (Bearer 형식)", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {
        String refreshToken = authHeader.substring(7);
        TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken, deviceId);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(
            summary = "DEV 로그인 (completed)",
            description = "개발용 로그인. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.",
            parameters = {
                    @Parameter(
                            name = "X-Device-ID",
                            description = "디바이스 식별자 (앱 환경에서 사용)",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = false
                    ),
                    @Parameter(
                            name = "deviceId",
                            description = "디바이스 식별자 쿠키 (웹 환경에서 사용)",
                            in = ParameterIn.COOKIE,
                            schema = @Schema(type = "string"),
                            required = false
                    )
            }
    )
    @ApiResponses( @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class))))
    @PostMapping("/dev/login")
    public ResponseEntity<LoginResponse> devlogin(
            @Parameter(description = "로그인 정보", required = true) @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {
        LoginResponse loginResponse = authService.devLogin(request, deviceId);
        return ResponseEntity.ok(loginResponse);
    }
}