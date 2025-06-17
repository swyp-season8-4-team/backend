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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.dto.response.LoginResponse;
import org.swyp.dessertbee.auth.dto.request.OAuthCodeRequest;
import org.swyp.dessertbee.auth.dto.request.AppleLoginRequest;
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
     * OAuth 로그인 처리 (웹: 인가 코드, 앱: 액세스 토큰)
     */
    @Operation(
            summary = "OAuth 회원가입, 로그인 (completed)",
            description = """
                    OAuth로 새로운 사용자 등록 및 로그인.
                    
                    **웹 환경**: OAuth 인가 코드를 code 필드에 전송
                    **앱 환경**: SDK에서 획득한 액세스 토큰을 code 필드에 전송하고 Platform-Type 헤더를 'app'으로 설정
                    
                    앱에서는 X-Device-ID 헤더를, 웹에서는 deviceId 쿠키를 사용하여 디바이스를 식별합니다.
                    """,
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
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "OAuth 요청 정보 (웹: 인가 코드, 앱: 액세스 토큰)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OAuthCodeRequest.class)
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "로그인 및 회원가입 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_PROVIDER, ErrorCode.DUPLICATE_NICKNAME})
    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> oauthCallback(
            @RequestBody OAuthCodeRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @Parameter(hidden = true) @RequestHeader(value = "Platform-Type", defaultValue = "web") String platformType
    ) {
        boolean isApp = "app".equalsIgnoreCase(platformType);
        log.info("OAuth 인가 코드 수신 - 제공자: {}, 플랫폼: {}", request.getProvider(), platformType);

        LoginResponse loginResponse = oAuthService.processOAuthLogin(
                request.getCode(), request.getProvider(), deviceId, isApp);

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Apple 특화 OAuth 인증 처리 (POST 요청)
     */
    @Operation(
            summary = "Apple 회원가입, 로그인 (completed)",
            description = "Apple ID로 새로운 사용자 등록 및 로그인. id_token과 사용자 정보를 함께 처리합니다. 웹과 앱의 처리가 다릅니다. 앱으로 처리할 시 반드시 Platform-Type 정보를 넘겨주셔야 합니다.",
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
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Apple 로그인 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppleLoginRequest.class)
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "로그인 및 회원가입 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_PROVIDER, ErrorCode.DUPLICATE_NICKNAME})
    @PostMapping("/apple/callback")
    public ResponseEntity<LoginResponse> appleCallback(
            @RequestBody AppleLoginRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "X-Device-ID", required = false) String deviceId,
            @Parameter(hidden = true) @RequestHeader(value = "Platform-Type", defaultValue = "web") String platformType
    ) {
        boolean isApp = "app".equalsIgnoreCase(platformType);
        log.info("Apple OAuth 인가 코드 수신");
        LoginResponse loginResponse = oAuthService.processAppleLogin(
                request.getCode(), request.getIdToken(), request.getState(),
                request.getUserInfo(), deviceId, isApp);

        return ResponseEntity.ok(loginResponse);
    }
}