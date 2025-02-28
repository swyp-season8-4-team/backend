package org.swyp.dessertbee.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.dto.oauth2.OAuthCodeRequest;
import org.swyp.dessertbee.auth.service.OAuthService;

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
    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> oauthCallback(
            @RequestBody OAuthCodeRequest request,
            HttpServletResponse response) {

        log.info("OAuth 인가 코드 수신 - 제공자: {}", request.getProvider());
        LoginResponse loginResponse = oAuthService.processOAuthLogin(
                request.getCode(), request.getProvider(), response);

        return ResponseEntity.ok(loginResponse);
    }
}