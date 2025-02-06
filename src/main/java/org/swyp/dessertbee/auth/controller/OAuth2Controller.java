package org.swyp.dessertbee.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.swyp.dessertbee.auth.dto.OAuth2AuthorizationRequest;

/**
 * OAuth2 인증 프로세스를 처리하는 컨트롤러
 * API 스펙에 맞춰 엔드포인트 재구성
 */
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2", description = "소셜 로그인 관련 API")
public class OAuth2Controller {

    // Kakao OAuth2 설정값들
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String kakaoAuthorizationUri;

    /**
     * 소셜 로그인 인증 시작점
     * POST /api/oauth2/authorization
     * @param request OAuth2 인증 요청 정보
     * @return 인증 페이지 URL
     */
    @PostMapping("/authorization")
    public ResponseEntity<?> socialLogin(
            @Valid @RequestBody OAuth2AuthorizationRequest request) {

        String authorizationUrl;
        String provider = request.getProvider().toLowerCase();

        // provider에 따른 URL 생성
        switch (provider) {
            case "kakao" -> {
                authorizationUrl = String.format("%s?client_id=%s&redirect_uri=%s&response_type=code",
                        kakaoAuthorizationUri,
                        kakaoClientId,
                        kakaoRedirectUri);
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        // 302 Found 상태코드와 함께 Location 헤더에 인증 URL 포함하여 반환
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorizationUrl)
                .build();
    }
}