package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.request.AppleLoginRequest.AppleUserInfo;
import org.swyp.dessertbee.auth.dto.response.LoginResponse;
import org.swyp.dessertbee.auth.enums.AuthProvider;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.auth.exception.OAuthExceptions.*;

/**
 * OAuth 인증 처리를 담당하는 공통 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final AppleOAuthService appleOAuthService;

    /**
     * 인가 코드로 OAuth 로그인 처리
     * 제공자에 따라 적절한 서비스로 위임
     *
     * @param code 인가 코드
     * @param providerName OAuth 제공자 (kakao, naver, google 등)
     * @param deviceId 디바이스 식별자
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processOAuthLogin(String code, String providerName, String deviceId) {
        try {
            // 문자열을 AuthProvider enum으로 변환
            AuthProvider provider = AuthProvider.fromString(providerName);

            if (provider == null) {
                throw new InvalidProviderException("OAuth 제공자가 입력되지 않았습니다.");
            }

            // enum을 사용하여 적절한 서비스 호출
            return switch (provider) {
                case KAKAO -> kakaoOAuthService.processKakaoLogin(code, deviceId);
                case APPLE -> appleOAuthService.processAppleLogin(code, null, null, null, deviceId);
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

    /**
     * Apple 로그인 처리를 위한 확장 메소드
     * Apple은 추가 파라미터(idToken, state, userInfo)가 필요하므로 별도 메소드 제공
     *
     * @param code 인가 코드
     * @param idToken Apple ID 토큰
     * @param state CSRF 방지용 상태값
     * @param userInfo 사용자 정보 (최초 로그인 시에만)
     * @param deviceId 디바이스 식별자
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processAppleLogin(String code, String idToken, String state, AppleUserInfo userInfo, String deviceId) {
        try {
            return appleOAuthService.processAppleLogin(code, idToken, state, userInfo, deviceId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 로그인 처리 중 오류 발생", e);
            throw new OAuthAuthenticationException("Apple 로그인 처리 중 오류가 발생했습니다.");
        }
    }
}