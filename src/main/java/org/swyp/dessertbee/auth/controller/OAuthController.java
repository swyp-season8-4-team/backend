package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * OAuth 인가 코드로 로그인 처리 (POST 요청)
     */
    @Operation(summary = "OAuth 회원가입, 로그인", description = "OAuth로 새로운 사용자 등록 및 로그인")
    @ApiResponse( responseCode = "200", description = "로그인 및 회원가입 성공", content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiErrorResponses({ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_FAILED, ErrorCode.INVALID_PROVIDER, ErrorCode.DUPLICATE_NICKNAME})
    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> oauthCallback(
            @RequestBody OAuthCodeRequest request) {

        log.info("OAuth 인가 코드 수신 - 제공자: {}", request.getProvider());
        LoginResponse loginResponse = oAuthService.processOAuthLogin(
                request.getCode(), request.getProvider());

        return ResponseEntity.ok(loginResponse);
    }
}