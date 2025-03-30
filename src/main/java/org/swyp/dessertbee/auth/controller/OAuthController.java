package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.oauth2.OAuthCodeRequest;
import org.swyp.dessertbee.auth.service.OAuthService;
import org.swyp.dessertbee.common.annotation.ApiErrorResponses;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * OAuth 인증을 처리하는 컨트롤러
 * 프론트엔드에서 받은 인가 코드로 OAuth 인증 처리
 */
@Tag(name = "OAuth", description = "소셜 인증 관련 API")
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * OAuth 인가 코드로 로그인 처리 (POST 요청)
     */
    @Operation(
            summary = "OAuth 회원가입, 로그인",
            description = "OAuth로 새로운 사용자 등록 및 로그인. 앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.",
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
    @ApiResponse(
            responseCode = "200",
            description = "로그인 및 회원가입 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)),
            headers = {
                    @Header(
                            name = "Set-Cookie",
                            description = "디바이스 식별자 쿠키 (웹 환경용)",
                            schema = @Schema(type = "string", example = "deviceId=abc123; Path=/; HttpOnly; Secure; SameSite=None")
                    )
            }
    )
    @ApiErrorResponses({ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_PROVIDER, ErrorCode.DUPLICATE_NICKNAME})
    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> oauthCallback(
            @RequestBody OAuthCodeRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        log.info("OAuth 인가 코드 수신 - 제공자: {}", request.getProvider());
        LoginResponse loginResponse = oAuthService.processOAuthLogin(
                request.getCode(), request.getProvider(), httpRequest, httpResponse);

        return ResponseEntity.ok(loginResponse);
    }
}