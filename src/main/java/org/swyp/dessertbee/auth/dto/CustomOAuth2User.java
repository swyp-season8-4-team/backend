
package org.swyp.dessertbee.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuth2Response;
    private final List<String> roles; // 단일 역할이 아닌 여러 역할

    public CustomOAuth2User(OAuth2Response oAuth2Response, List<String> roles
    ) {
        this.oAuth2Response = oAuth2Response;
        this.roles = roles;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(() -> role);
        }
        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2Response.getProvider()+"_"+oAuth2Response.getProviderId() + "_" + oAuth2Response.getNickname();
    }

    public String getNickname() {
        return oAuth2Response.getNickname();
    }

    public String getEmail() {
        return oAuth2Response.getEmail();
    }
}