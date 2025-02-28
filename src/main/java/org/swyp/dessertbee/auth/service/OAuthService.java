package org.swyp.dessertbee.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;

/**
 * OAuth 인증 처리를 담당하는 공통 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KakaoOAuthService kakaoOAuthService;
    // 추후 다른 OAuth 서비스 추가 (네이버, 구글 등)

    /**
     * 인가 코드로 OAuth 로그인 처리
     * 제공자에 따라 적절한 서비스로 위임
     *
     * @param code 인가 코드
     * @param provider OAuth 제공자 (kakao, naver, google 등)
     * @param response HTTP 응답
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processOAuthLogin(String code, String provider, HttpServletResponse response) {
        try {
            // 제공자에 따라 적절한 서비스 호출
            switch (provider.toLowerCase()) {
                case "kakao":
                    return kakaoOAuthService.processKakaoLogin(code, response);
                // 추후 다른 OAuth 제공자 추가
                default:
                    throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                            "지원하지 않는 OAuth 제공자입니다: " + provider);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth 로그인 처리 중 오류 발생 - 제공자: {}", provider, e);
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED,
                    "OAuth 로그인 처리 중 오류가 발생했습니다.");
        }
    }
}