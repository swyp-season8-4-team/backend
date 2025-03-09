package org.swyp.dessertbee.auth.dto.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;
@Slf4j
public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    public KakaoResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
        // kakao_account와 profile 정보 추출
        this.kakaoAccount = getKakaoAccount();
        this.profile = getProfile();
    }

    private Map<String, Object> getKakaoAccount() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        if (account == null) {
            log.error("OAuth2 카카오 응답에 kakao_account가 없습니다.");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_response", "Missing kakao_account data", null)
            );
        }
        return account;
    }

    private Map<String, Object> getProfile() {
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            log.error("OAuth2 카카오 응답에 profile이 없습니다.");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_response", "Missing profile data", null)
            );
        }
        return profile;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getImageUrl() {
        return (String) profile.get("profile_image_url");
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getEmail() {
        return kakaoAccount.get("email").toString();
    }

    @Override
    public String getNickname() {
        return profile.get("nickname").toString();
    }
}
