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
     * OAuth 로그인 처리 (웹: 인가 코드, 앱: 액세스 토큰)
     * 제공자와 플랫폼에 따라 적절한 서비스로 위임
     *
     * @param codeOrToken 웹: 인가 코드, 앱: 액세스 토큰
     * @param providerName OAuth 제공자 (kakao, naver, google 등)
     * @param deviceId 디바이스 식별자
     * @param isApp 앱 환경 여부 (true: 액세스 토큰, false: 인가 코드)
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processOAuthLogin(String codeOrToken, String providerName, String deviceId, boolean isApp) {
        try {
            // 문자열을 AuthProvider enum으로 변환
            AuthProvider provider = AuthProvider.fromString(providerName);

            if (provider == null) {
                throw new InvalidProviderException("OAuth 제공자가 입력되지 않았습니다.");
            }

            // enum과 플랫폼을 사용하여 적절한 서비스 호출
            return switch (provider) {
                case KAKAO -> {
                    if (isApp) {
                        // 앱: 액세스 토큰으로 직접 처리
                        log.info("카카오 앱 로그인 - 액세스 토큰 방식");
                        yield kakaoOAuthService.processKakaoTokenLogin(codeOrToken, deviceId, isApp);
                    } else {
                        // 웹: 인가 코드로 처리
                        log.info("카카오 웹 로그인 - 인가 코드 방식");
                        yield kakaoOAuthService.processKakaoLogin(codeOrToken, deviceId, isApp);
                    }
                }
                case APPLE -> appleOAuthService.processAppleLogin(codeOrToken, null, null, null, deviceId, isApp);
                // 추후 다른 OAuth 제공자 추가
                default -> throw new InvalidProviderException("아직 구현되지 않은 OAuth 제공자입니다: " + provider.getProviderName());
            };
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OAuth 로그인 처리 중 오류 발생 - 제공자: {}, 플랫폼: {}", providerName, isApp ? "APP" : "WEB", e);
            throw new OAuthAuthenticationException("OAuth 로그인 처리 중 오류가 발생했습니다.");
        }
    }


    /**
     * 카카오 액세스 토큰으로 로그인 처리 (Flutter 앱용)
     * Flutter에서 카카오 SDK로 획득한 액세스 토큰을 직접 처리
     *
     * @param accessToken 카카오 액세스 토큰
     * @param deviceId 디바이스 식별자
     * @param isApp 앱 환경 여부
     * @return 로그인 응답 (JWT 토큰 포함)
     */
    @Transactional
    public LoginResponse processKakaoTokenLogin(String accessToken, String deviceId, boolean isApp) {
        try {
            log.info("카카오 액세스 토큰 로그인 처리 시작 - 플랫폼: {}", isApp ? "APP" : "WEB");
            return kakaoOAuthService.processKakaoTokenLogin(accessToken, deviceId, isApp);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 로그인 처리 중 오류 발생", e);
            throw new OAuthAuthenticationException("카카오 토큰 로그인 처리 중 오류가 발생했습니다.");
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
    public LoginResponse processAppleLogin(String code, String idToken, String state, AppleUserInfo userInfo, String deviceId, boolean isApp) {
        try {
            return appleOAuthService.processAppleLogin(code, idToken, state, userInfo, deviceId, isApp);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Apple 로그인 처리 중 오류 발생 - 플랫폼: {}", isApp ? "APP" : "WEB", e);
            throw new OAuthAuthenticationException("Apple 로그인 처리 중 오류가 발생했습니다.");
        }
    }
}