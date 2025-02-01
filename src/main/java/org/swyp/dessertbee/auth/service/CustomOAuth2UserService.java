package org.swyp.dessertbee.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.dto.KakaoResponse;
import org.swyp.dessertbee.auth.dto.OAuth2Response;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.*;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (attributes == null || attributes.isEmpty()) {
            throw new OAuth2AuthenticationException("Failed to retrieve user attributes from OAuth2 provider.");
        }

        System.out.println("User Attributes: " + attributes); // 테스트 로그

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuthResponse;

        if ("kakao".equals(registrationId)) { // 일단 카카오만
            oAuthResponse = new KakaoResponse(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        UserEntity existData = userRepository.findByEmail(oAuthResponse.getEmail());
        String role = "ROLE_USER";

        if (existData == null) { // 기존 유저가 없으면 회원가입 진행
            UserEntity userEntity = new UserEntity();
            userEntity.setEmail(oAuthResponse.getEmail());
            userEntity.setNickname(oAuthResponse.getNickname());
            userEntity.setRole(role);

            userRepository.save(userEntity);
        } else { // 기존 유저가 있으면 닉네임 업데이트
            existData.setNickname(oAuthResponse.getNickname());
            role = existData.getRole();
            userRepository.save(existData);
        }
        return new CustomOAuth2User(oAuthResponse, role);
    }

}
