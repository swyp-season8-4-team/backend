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

import java.util.Optional;

/**
 * OAuth 계정 통합을 처리하는 서비스
 * 동일한 이메일로 다른 OAuth 제공자에서 가입할 때 계정을 연결하는 로직을 담당
 */
public interface OAuthAccountLinkingService {
    
    /**
     * OAuth 로그인 시 기존 사용자 조회
     *
     * @param oauth2Response OAuth 응답 정보
     * @return 사용자 엔티티 (새로운 사용자인 경우 ID가 null)
     */
    UserEntity findOrCreateUser(OAuth2Response oauth2Response);

    /**
     * 기존 사용자에게 새로운 OAuth 제공자 연결
     *
     * @param user 기존 사용자
     * @param oauth2Response OAuth 응답 정보
     * @param deviceId 디바이스 ID
     * @param isApp 앱 환경 여부
     * @return 연결된 사용자 엔티티
     */
    UserEntity linkOAuthProviderToUser(UserEntity user, OAuth2Response oauth2Response, String deviceId, boolean isApp);
}

@Service
@RequiredArgsConstructor
@Slf4j
class OAuthAccountLinkingServiceImpl implements OAuthAccountLinkingService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    @Override
    @Transactional(readOnly = true)
    public UserEntity findOrCreateUser(OAuth2Response oauth2Response) {
        String email = oauth2Response.getEmail();
        String provider = oauth2Response.getProvider();

        log.info("OAuth 사용자 조회 시작 - 이메일: {}, 제공자: {}", email, provider);

        // 1. 동일한 OAuth 제공자로 가입된 사용자 조회
        Optional<UserEntity> existingUserWithSameProvider = userRepository.findByEmailAndOAuthProvider(email, provider);
        if (existingUserWithSameProvider.isPresent()) {
            log.info("동일한 OAuth 제공자로 가입된 사용자 발견 - 이메일: {}, 제공자: {}", email, provider);
            return existingUserWithSameProvider.get();
        }

        // 2. 이메일로 기존 사용자 조회 (다른 OAuth 제공자 또는 일반 가입)
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            log.info("동일한 이메일로 가입된 기존 사용자 발견 - 이메일: {}, 제공자: {}", email, provider);
            return existingUser.get();
        }

        // 3. 새로운 사용자 생성이 필요한 경우
        log.info("새로운 사용자 생성 필요 - 이메일: {}, 제공자: {}", email, provider);
        return UserEntity.builder()
                .email(email)
                .nickname(oauth2Response.getNickname())
                .build();
    }

    @Override
    @Transactional
    public UserEntity linkOAuthProviderToUser(UserEntity user, OAuth2Response oauth2Response, String deviceId, boolean isApp) {
        String provider = oauth2Response.getProvider();
        String providerId = oauth2Response.getProviderId();

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

        log.info("OAuth 제공자 자동 연결 완료 - 사용자 ID: {}, 제공자: {}", user.getId(), provider);
        return user;
    }
} 