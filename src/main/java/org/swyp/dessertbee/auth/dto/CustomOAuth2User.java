package org.swyp.dessertbee.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuthResponse;
    private final String role;

    public CustomOAuth2User(OAuth2Response oAuthResponse, String role) {
        this.oAuthResponse = oAuthResponse;
        this.role = role;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return role;
            }
        });

        return collection;
    }

    @Override
    public String getName() {
        return oAuthResponse.getName();
    }

    public String getUserName() {
        return oAuthResponse.getProvider()+ " " + oAuthResponse.getProviderId();
    }
}
