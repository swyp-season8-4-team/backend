package org.swyp.dessertbee.auth.dto.userdetails;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Spring Security의 인증 객체를 표현하는 클래스
 * DB에서 조회한 사용자 정보를 기반으로 UserDetails를 구현
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final String email;
    private final List<GrantedAuthority> authorities;
    private final UUID userUuid;

    public CustomUserDetails(String email, List<String> roleNames, UUID userUuid) {
        this.email = email;
        this.userUuid = userUuid;
        this.authorities = roleNames.stream()
                .map(role -> (GrantedAuthority) () -> role) // SimpleGrantedAuthority 대체
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // JWT 기반 인증이므로 비밀번호 불필요
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}