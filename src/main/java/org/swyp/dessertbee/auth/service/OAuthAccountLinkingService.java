package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.oauth2.OAuth2Response;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/**
 * OAuth 계정 통합을 처리하는 서비스
 * 동일한 이메일로 다른 OAuth 제공자에서 가입할 때 계정을 연결하는 로직을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthAccountLinkingService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    // 계정 연결 상태를 추적하기 위한 ThreadLocal 변수
    private final ThreadLocal<Boolean> accountLinkingOccurred = ThreadLocal.withInitial(() -> false);

    /**
     * OAuth 로그인 시 기존 사용자 조회 및 자동 계정 통합 처리
     *
     * @param oauth2Response OAuth 응답 정보
     * @param deviceId 디바이스 ID
     * @param isApp 앱 환경 여부
     * @return 기존 사용자 또는 새로 생성된 사용자
     */
    @Transactional
    public UserEntity processOAuthUserLogin(OAuth2Response oauth2Response, String deviceId, boolean isApp) {
        String email = oauth2Response.getEmail();
        String provider = oauth2Response.getProvider();
        String providerId = oauth2Response.getProviderId();

        log.info("OAuth 자동 계정 통합 처리 시작 - 이메일: {}, 제공자: {}", email, provider);

        // 1. 동일한 OAuth 제공자로 가입된 사용자 조회
        Optional<UserEntity> existingUserWithSameProvider = userRepository.findByEmailAndOAuthProvider(email, provider);
        if (existingUserWithSameProvider.isPresent()) {
            log.info("동일한 OAuth 제공자로 가입된 사용자 발견 - 이메일: {}, 제공자: {}", email, provider);
            return existingUserWithSameProvider.get();
        }

        // 2. 이메일로 기존 사용자 조회 (다른 OAuth 제공자 또는 일반 가입)
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            List<String> existingProviders = userRepository.findOAuthProvidersByEmail(email);
            
            log.info("동일한 이메일로 가입된 기존 사용자 발견 - 자동 계정 연결 시작 - 이메일: {}, 기존 제공자들: {}, 새로운 제공자: {}", 
                    email, existingProviders, provider);

            // 3. 자동으로 기존 사용자에게 새로운 OAuth 제공자 연결
            try {
                UserEntity linkedUser = linkOAuthProviderToExistingUser(user, provider, providerId, deviceId, isApp);
                log.info("OAuth 계정 자동 연결 완료 - 사용자 ID: {}, 이메일: {}, 연결된 제공자: {}", 
                        linkedUser.getId(), email, provider);
                
                return linkedUser;
            } catch (Exception e) {
                log.error("OAuth 계정 자동 연결 실패 - 이메일: {}, 제공자: {}, 오류: {}", email, provider, e.getMessage());
                throw new BusinessException(ErrorCode.OAUTH_ACCOUNT_LINKING_FAILED, 
                        "동일한 이메일로 가입된 계정과 자동 연결에 실패했습니다.");
            }
        }

        // 4. 새로운 사용자 생성이 필요한 경우 (ID가 null인 UserEntity 반환)
        log.info("새로운 사용자 생성 필요 - 이메일: {}, 제공자: {}", email, provider);
        return UserEntity.builder()
                .email(email)
                .nickname(oauth2Response.getNickname())
                .build();
    }

    /**
     * 기존 사용자에게 새로운 OAuth 제공자 자동 연결
     */
    private UserEntity linkOAuthProviderToExistingUser(UserEntity user, String provider, String providerId, 
                                                      String deviceId, boolean isApp) {
        log.info("기존 사용자에게 OAuth 제공자 자동 연결 시작 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);

        // 이미 해당 제공자로 연결되어 있는지 확인
        Optional<AuthEntity> existingAuth = authRepository.findByProviderAndProviderId(provider, providerId);
        if (existingAuth.isPresent()) {
            log.info("이미 해당 OAuth 제공자로 연결된 계정 존재 - 제공자: {}, 제공자 ID: {}", provider, providerId);
            return user;
        }

        // 사용자가 이미 해당 제공자로 가입되어 있는지 확인
        boolean alreadyHasProvider = user.getAuthEntities().stream()
                .anyMatch(auth -> auth.getProvider().equals(provider));
        if (alreadyHasProvider) {
            log.info("사용자가 이미 해당 OAuth 제공자로 가입되어 있음 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);
            return user;
        }

        // 기존 사용자에게 새로운 OAuth 제공자 자동 연결
        AuthEntity newAuth = AuthEntity.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .deviceId(deviceId)
                .active(true)
                .build();

        authRepository.save(newAuth);
        user.getAuthEntities().add(newAuth);

        // 새로운 OAuth 제공자가 실제로 연결되었을 때만 계정 연결 상태를 true로 설정
        accountLinkingOccurred.set(true);

        log.info("OAuth 제공자 자동 연결 완료 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);
        return user;
    }

    /**
     * 사용자의 OAuth 제공자 목록 조회
     */
    public List<String> getUserOAuthProviders(String email) {
        return userRepository.findOAuthProvidersByEmail(email);
    }

    /**
     * 사용자가 특정 OAuth 제공자로 가입했는지 확인
     */
    public boolean hasOAuthProvider(String email, String provider) {
        return userRepository.findByEmailAndOAuthProvider(email, provider).isPresent();
    }

    /**
     * 사용자의 OAuth 계정 수 조회
     */
    public long getOAuthProviderCount(String email) {
        return userRepository.countOAuthProvidersByEmail(email);
    }

    /**
     * 현재 요청에서 계정 연결이 발생했는지 확인
     */
    public boolean isAccountLinkingOccurred() {
        return accountLinkingOccurred.get();
    }

    /**
     * 계정 연결 상태 초기화 (요청 처리 완료 후 호출)
     */
    public void resetAccountLinkingStatus() {
        accountLinkingOccurred.set(false);
    }

    /**
     * 계정 연결 정보 조회
     */
    public Map<String, Object> getAccountLinkingInfo(String email) {
        Map<String, Object> info = new HashMap<>();
        info.put("email", email);
        info.put("providers", getUserOAuthProviders(email));
        info.put("providerCount", getOAuthProviderCount(email));
        info.put("hasMultipleProviders", getOAuthProviderCount(email) > 1);
        return info;
    }
} 