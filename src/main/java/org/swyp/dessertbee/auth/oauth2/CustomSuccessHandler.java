package org.swyp.dessertbee.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.jwt.JWTUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // OAuth2User 정보 가져오기
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String email = customUserDetails.getEmail();

        // 권한(roles) 가져오기 → "ROLE_" 접두어 제거
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // "ROLE_ADMIN" → "ADMIN"
                .collect(Collectors.toList());

        // JWT 생성
        String token = jwtUtil.createJwt(email, roles, 60 * 60 * 60L);

        // 쿠키에 JWT 추가 & 리다이렉트
        response.addCookie(createCookie(token));
        response.sendRedirect("http://localhost:3000/");
    }

    private Cookie createCookie(String value) {
        Cookie cookie = new Cookie("Authorization", value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
