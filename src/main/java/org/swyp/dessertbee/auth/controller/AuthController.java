package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.dto.request.SignUpWithProfileRequest;
import org.swyp.dessertbee.statistics.user.service.UserStatisticsAdminService;
import org.swyp.dessertbee.auth.dto.request.PasswordResetRequest;
import org.swyp.dessertbee.auth.dto.response.PasswordResetResponse;
import org.swyp.dessertbee.auth.dto.response.TokenResponse;
import org.swyp.dessertbee.auth.dto.request.LoginRequest;
import org.swyp.dessertbee.auth.dto.response.LoginResponse;
import org.swyp.dessertbee.auth.dto.response.LogoutResponse;
import org.swyp.dessertbee.auth.dto.request.SignUpRequest;
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
    private final UserStatisticsAdminService userStatisticsAdminService;

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
                    ),
                    @Parameter(
                            name = "X-Email-Verification-Token",
                            description = "이메일 인증 토큰",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = true
                    )
            }
    )
    @ApiResponse( responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)) )
    @ApiErrorResponses({ErrorCode.PASSWORD_MISMATCH, ErrorCode.DUPLICATE_NICKNAME, ErrorCode.DUPLICATE_EMAIL})
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @RequestHeader("X-Email-Verification-Token") String verificationToken,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @Parameter(
                    description = "회원가입 정보",
                    required = true,
                    schema = @Schema(implementation = SignUpRequest.class)
            ) @Valid @RequestBody SignUpRequest request
    ) {
        log.debug("회원가입 요청: {}", request.getEmail());
        LoginResponse signupResponse = authService.signup(request, verificationToken, deviceId);
        return ResponseEntity.ok(signupResponse);
    }

    // 프로필 이미지 포함한 회원가입 API (Multipart)
    @Operation(
            summary = "프로필 이미지 포함 회원가입",
            description = "새로운 사용자를 등록합니다. 프로필 이미지를 함께 업로드할 수 있습니다. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다. 프로필 이미지는 JPG, JPEG, PNG, GIF 형식의 5MB 이하 파일만 허용됩니다.",
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
                    ),
                    @Parameter(
                            name = "X-Email-Verification-Token",
                            description = "이메일 인증 토큰",
                            in = ParameterIn.HEADER,
                            schema = @Schema(type = "string"),
                            required = true
                    )
            }
    )
    @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiErrorResponses({ ErrorCode.PASSWORD_MISMATCH, ErrorCode.DUPLICATE_NICKNAME, ErrorCode.DUPLICATE_EMAIL, ErrorCode.INVALID_VERIFICATION_TOKEN, ErrorCode.INVALID_FILE_TYPE, ErrorCode.FILE_SIZE_EXCEEDED, ErrorCode.FILE_UPLOAD_ERROR})
    @PostMapping(value = "/signup-with-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoginResponse> signupWithProfile(
            @RequestHeader("X-Email-Verification-Token") String verificationToken,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @Valid @ModelAttribute SignUpWithProfileRequest request
    ) {
        log.debug("프로필 이미지 포함 회원가입 요청: {}", request.getEmail());
        LoginResponse signupResponse = authService.signupWithProfileImage(request, request.getProfileImage(), verificationToken, deviceId);
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
    @ApiErrorResponses({ErrorCode.PASSWORD_MISMATCH, ErrorCode.USER_NOT_FOUND, ErrorCode.ACCOUNT_LOCKED})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(
                    description = "로그인 정보",
                    required = true,
                    schema = @Schema(implementation = LoginRequest.class)
            )  @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {

        LoginResponse loginResponse = authService.login(request, deviceId, false);
      
       // ✅ 활성 사용자 기록
        userStatisticsAdminService.trackUserActivity(String.valueOf(loginResponse.getUserUuid()));

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
    @ApiErrorResponses({ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_VERIFICATION_TOKEN, ErrorCode.JWT_TOKEN_EXPIRED, ErrorCode.DEVICE_ID_MISSING})
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
            description = "개발용 로그인. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.(리프레시 토큰 10분, 액세스 토큰 3분)",
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
    @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                    schema = @Schema(implementation = LoginResponse.class)
            )
    )
    @ApiErrorResponses({ErrorCode.PASSWORD_MISMATCH, ErrorCode.USER_NOT_FOUND})
    @PostMapping("/dev/login")
    public ResponseEntity<LoginResponse> devlogin(
            @Parameter(
                    description = "로그인 정보",
                    required = true,
                    schema = @Schema(implementation = LoginRequest.class)
            )  @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId
    ) {
        LoginResponse loginResponse = authService.login(request, deviceId, true);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "이메일 인증 토큰을 사용하여 비밀번호를 재설정합니다. 재설정이 성공하면 모든 디바이스에서 로그아웃됩니다.",
            parameters = {
                    @Parameter(
                            name = "X-Email-Verification-Token",
                            description = "이메일 인증 토큰 (분실한 비밀번호 찾기 요청 시 발급됨)",
                            in = ParameterIn.HEADER,
                            required = true,
                            schema = @Schema(type = "string")
                    )
            }
    )
    @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공")
    @ApiErrorResponses({ErrorCode.INVALID_VERIFICATION_TOKEN, ErrorCode.JWT_TOKEN_EXPIRED, ErrorCode.USER_NOT_FOUND})
    @PostMapping("/password/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Parameter(description = "이메일 인증 토큰", required = true)
            @RequestHeader("X-Email-Verification-Token") String verificationToken,
            @Parameter(
                    description = "비밀번호 재설정 정보",
                    required = true,
                    schema = @Schema(implementation = PasswordResetRequest.class)
            )
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request, verificationToken);
        return ResponseEntity.ok().build();
    }

}