package org.swyp.dessertbee.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.dto.KakaoResponse;
import org.swyp.dessertbee.auth.dto.OAuth2Response;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (attributes == null || attributes.isEmpty()) {
            throw new OAuth2AuthenticationException("Failed to retrieve user attributes from OAuth2 provider.");
        }

        System.out.println("User Attributes: " + attributes);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuthResponse;

        if ("kakao".equals(registrationId)) {
            oAuthResponse = new KakaoResponse(attributes);
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
        }

        String role = "ROLE_USER";
        return new CustomOAuth2User(oAuthResponse, role);
    }

}
