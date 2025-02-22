package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.dto.login.LoginRequest;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.logout.LogoutResponse;
import org.swyp.dessertbee.auth.dto.passwordreset.PasswordResetRequest;
import org.swyp.dessertbee.auth.dto.signup.SignUpRequest;
import org.swyp.dessertbee.auth.dto.signup.SignUpResponse;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.service.AuthService;
import jakarta.validation.Valid;

@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JWTUtil jwtUtil;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @RequestHeader("X-Email-Verification-Token") String verificationToken,
            @Valid @RequestBody SignUpRequest request
    ) throws BadRequestException {
        log.debug("회원가입 요청: {}", request.getEmail());

        // 비밀번호 일치 여부 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("비밀번호가 일치하지 않습니다.");
        }

        // 회원가입 처리
        LoginResponse response = authService.signup(request, verificationToken);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "로그인 정보", required = true)
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @Parameter(description = "JWT 액세스 토큰", required = true)
            @RequestHeader("Authorization") String token
    ) {
        String accessToken = token.substring(7);
        return ResponseEntity.ok(authService.logout(accessToken));
    }

    @Operation(summary = "비밀번호 재설정", description = "이메일 인증 후 비밀번호를 재설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "이메일 인증 토큰", required = true)
            @RequestHeader("X-Email-Verification-Token") String verificationToken,
            @Parameter(description = "비밀번호 재설정 정보", required = true)
            @Valid @RequestBody PasswordResetRequest request
    ) {
        authService.resetPassword(request, verificationToken);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 재발급", description = "Refresh 토큰을 사용하여 새로운 Access 토큰을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh 토큰")
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestHeader("Authorization") String bearerToken) {
        try {
            // Bearer 토큰에서 만료된 액세스 토큰 추출
            String accessToken = bearerToken.substring(7);  // "Bearer " 제거

            // 토큰에서 이메일 추출
            String email = jwtUtil.getEmail(accessToken, true);

            // 이메일로 새로운 액세스 토큰 발급
            TokenResponse tokenResponse = authService.refreshAccessToken(email);

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

