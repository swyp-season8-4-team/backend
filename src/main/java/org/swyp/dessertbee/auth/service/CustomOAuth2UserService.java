package org.swyp.dessertbee.auth.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final RoleRepository roleRepository;
    private final AuthRepository authRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = switch (registrationId) {
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            // 다른 OAuth 제공자 추가 가능
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        };

        UserEntity user = userRepository.findByEmail(oAuth2Response.getEmail());
        RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        if (user == null) {
            user = createNewUser(oAuth2Response, userRole);
        }

        updateOrCreateAuthEntity(user, registrationId, oAuth2Response);

        return new CustomOAuth2User(UserDTO.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .roles(userRoleService.getUserRoles(user))
                .build());
    }

    private UserEntity createNewUser(OAuth2Response oAuth2Response, RoleEntity userRole) {
        UserEntity newUser = UserEntity.builder()
                .email(oAuth2Response.getEmail())
                .nickname(oAuth2Response.getNickname())
                .build();
        newUser.addRole(userRole);
        return userRepository.save(newUser);
    }

    private void updateOrCreateAuthEntity(UserEntity user, String provider, OAuth2Response oAuth2Response) {
        AuthEntity auth = authRepository.findByUserAndProvider(user, provider)
                .orElse(AuthEntity.builder()
                        .user(user)
                        .provider(provider)
                        .build());

        auth.setProviderId(oAuth2Response.getProviderId());
        authRepository.save(auth);
    }
}