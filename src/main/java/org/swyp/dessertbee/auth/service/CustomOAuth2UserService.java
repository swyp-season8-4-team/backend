package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.dto.KakaoResponse;
import org.swyp.dessertbee.auth.dto.OAuth2Response;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.role.service.UserRoleService;
import org.swyp.dessertbee.user.dto.UserDTO;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OAuth2 사용자 정보를 처리하는 서비스
 */
/**
 * OAuth2 사용자 정보를 처리하는 서비스
 * DefaultOAuth2UserService를 상속받아 OAuth2 인증 프로세스를 커스터마이징
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        log.info("OAuth2 사용자 정보 로드 시작");

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 에러 발생", e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("processing_error", "Failed to process OAuth2 login", null)
            );
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // OAuth2 제공자 확인 (kakao)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("OAuth2 제공자: {}", provider);

        // OAuth2 사용자 정보 추출
        OAuth2Response oauth2Response = extractOAuth2UserInfo(provider, oauth2User.getAttributes());
        String email = oauth2Response.getEmail();
        log.info("OAuth2 사용자 이메일: {}", email);

        // 사용자 정보 저장 또는 업데이트
        UserEntity user = saveOrUpdateUser(oauth2Response);

        return createCustomOAuth2User(user, oauth2User.getAttributes());
    }

    private OAuth2Response extractOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "kakao" -> new KakaoResponse(attributes);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        };
    }

    @Transactional
    protected UserEntity saveOrUpdateUser(OAuth2Response oauth2Response) {
        return userRepository.findByEmail(oauth2Response.getEmail())
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .email(oauth2Response.getEmail())
                            .nickname(oauth2Response.getNickname())
                            .build();
                    log.info("새로운 OAuth2 사용자 생성: {}", oauth2Response.getEmail());
                    return userRepository.save(newUser);
                });
    }

    private CustomOAuth2User createCustomOAuth2User(UserEntity user, Map<String, Object> attributes) {
        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userUuid(user.getUserUuid())
                .roles(user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getName())
                        .collect(Collectors.toList()))
                .build();

        return new CustomOAuth2User(userDTO, attributes);
    }
}