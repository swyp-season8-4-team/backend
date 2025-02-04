
package org.swyp.dessertbee.auth.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.swyp.dessertbee.user.dto.UserDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OAuth2 인증이 완료된 사용자의 정보를 담는 클래스
 * Spring Security의 OAuth2User 인터페이스를 구현
 */
@Getter
public class CustomOAuth2User implements OAuth2User {
    private final String email;
    private final String nickname;
    private final UUID userUuid;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UserDTO userDTO, Map<String, Object> attributes) {
        this.email = userDTO.getEmail();
        this.nickname = userDTO.getNickname();
        this.userUuid = userDTO.getUserUuid();
        this.attributes = attributes;
        this.authorities = userDTO.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return this.email;
    }
}