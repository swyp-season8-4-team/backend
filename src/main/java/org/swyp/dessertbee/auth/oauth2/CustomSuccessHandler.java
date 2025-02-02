package org.swyp.dessertbee.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.service.AuthService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        // OAuth2User 정보 가져오기
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String email = customUserDetails.getEmail();

        // 권한(roles) 가져오기 → "ROLE_" 접두어 제거
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        // Access Token과 Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(email, roles);
        String refreshToken = jwtUtil.createRefreshToken(email, roles);

        // Refresh Token DB 저장
        authService.saveRefreshToken(email, refreshToken);

        // Access Token을 Authorization 헤더에 설정
        response.setHeader("Authorization", "Bearer " + accessToken);

        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);

        // CORS 헤더 설정
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 프론트엔드로 리다이렉트
        response.sendRedirect("http://localhost:3000/oauth2/redirect");
    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS만 사용
        return cookie;
    }
}