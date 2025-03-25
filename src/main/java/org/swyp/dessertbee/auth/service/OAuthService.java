package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.enums.AuthProvider;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.auth.exception.OAuthExceptions.*;
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
     * @param providerName OAuth 제공자 (kakao, naver, google 등)
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processOAuthLogin(String code, String providerName) {
        try {
            // 문자열을 AuthProvider enum으로 변환
            AuthProvider provider = AuthProvider.fromString(providerName);

            if (provider == null) {
                throw new InvalidProviderException("OAuth 제공자가 입력되지 않았습니다.");
            }

            // enum을 사용하여 적절한 서비스 호출
            return switch (provider) {
                case KAKAO -> kakaoOAuthService.processKakaoLogin(code);
                // 추후 다른 OAuth 제공자 추가
                default -> throw new InvalidProviderException("아직 구현되지 않은 OAuth 제공자입니다: " + provider.getProviderName());
            };
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth 로그인 처리 중 오류 발생 - 제공자: {}", providerName, e);
            throw new OAuthAuthenticationException("OAuth 로그인 처리 중 오류가 발생했습니다.");
        }
    }
}