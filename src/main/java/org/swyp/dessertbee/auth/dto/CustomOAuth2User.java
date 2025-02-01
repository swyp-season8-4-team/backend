
package org.swyp.dessertbee.auth.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.swyp.dessertbee.user.dto.UserDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserDTO userDTO;

    public CustomOAuth2User(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : userDTO.getRoles()) {
            authorities.add(() -> role);
        }
        return authorities;
    }

    @Override
    public String getName() {
        return userDTO.getNickname();
    }

    public String getEmail() {
        return userDTO.getEmail();
    }
}