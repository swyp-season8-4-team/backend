package org.swyp.dessertbee.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.dto.KakaoResponse;
import org.swyp.dessertbee.auth.dto.OAuth2Response;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.role.service.UserRoleService;
import org.swyp.dessertbee.user.dto.UserDTO;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final RoleRepository roleRepository;
    private final AuthRepository authRepository;


    public CustomOAuth2UserService(UserRepository userRepository,
                                   UserRoleService userRoleService,
                                   RoleRepository roleRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
        this.roleRepository = roleRepository;
        this.authRepository = authRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2 제공자로부터 사용자 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (attributes == null || attributes.isEmpty()) {
            throw new OAuth2AuthenticationException("Failed to retrieve user attributes from OAuth2 provider.");
        }

        System.out.println("User Attributes: " + attributes); // 테스트 로그

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuthResponse;

        if ("kakao".equals(registrationId)) { // 카카오만 지원
            oAuthResponse = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        // 이메일로 기존 사용자 조회
        UserEntity existData = userRepository.findByEmail(oAuthResponse.getEmail());

        // 기본 역할 ROLE_USER 조회
        RoleEntity roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new NoSuchElementException("Role not found: ROLE_USER"));

        UserDTO userDTO = new UserDTO();
        if (existData == null) { // 신규 사용자이면 회원가입 진행
            UserEntity userEntity = new UserEntity();
            userEntity.setEmail(oAuthResponse.getEmail());
            userEntity.setNickname(oAuthResponse.getNickname());
            // UserEntity에 기본 역할 추가 (연관관계에 cascade 옵션이 있으므로 user_role에도 저장됨)
            userEntity.addRole(roleUser);

            // 저장 후 반환값을 existData에 할당
            existData = userRepository.save(userEntity);
            userDTO.setEmail(oAuthResponse.getEmail());
            userDTO.setNickname(oAuthResponse.getNickname());
            userDTO.setRoles(userRoleService.getUserRoles(existData));


        } else { // 기존 사용자
            // 기존 사용자에 역할 매핑이 없는 경우 DB에 기본 역할 매핑 추가
            List<String> existingRoles = userRoleService.getUserRoles(existData);
            if (existingRoles.isEmpty()) {
                existData.addRole(roleUser);
                existData = userRepository.save(existData);
            }

            userDTO.setEmail(existData.getEmail());
            userDTO.setNickname(existData.getNickname());
            userDTO.setRoles(userRoleService.getUserRoles(existData));
        }
        updateOrCreateAuthEntity(existData, registrationId, oAuthResponse);
        return new CustomOAuth2User(userDTO);

    }

    private void updateOrCreateAuthEntity(UserEntity userEntity, String provider, OAuth2Response oAuthResponse) {
        // AuthEntity 조회
        AuthEntity authEntity = authRepository.findByUserAndProvider(userEntity, provider)
                .orElse(null);

        if (authEntity == null) {
            // AuthEntity가 없으면 생성
            authEntity = AuthEntity.builder()
                    .user(userEntity)
                    .provider(oAuthResponse.getProvider())
                    .providerId(oAuthResponse.getProviderId())
                    .build();
        } else {
            // 기존 AuthEntity 업데이트
            authEntity.setProviderId(oAuthResponse.getProviderId());
        }

        // 저장
        authRepository.save(authEntity);
    }
}
