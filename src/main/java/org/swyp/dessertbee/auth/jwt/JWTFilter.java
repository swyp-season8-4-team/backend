package org.swyp.dessertbee.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.dto.TokenResponse;
import org.swyp.dessertbee.auth.service.AuthService;
import org.swyp.dessertbee.user.dto.UserDTO;

import java.io.IOException;
import java.util.List;

/**
 * 사용자의 쿠키에서 JWT를 추출하여 인증을 수행하는 필터이다.
 */
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final AuthService authService;

    public JWTFilter(JWTUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = extractTokenFromHeader(request);
        String refreshToken = extractTokenFromCookie(request, "refresh_token");

        if (accessToken != null && jwtUtil.validateToken(accessToken, true)) {
            processToken(accessToken, true, request);
        } else if (refreshToken != null && jwtUtil.validateToken(refreshToken, false)) {
            // Refresh Token으로 새로운 Access Token 발급
            TokenResponse newTokens = authService.refreshTokens(refreshToken);
            if (newTokens != null) {
                addTokenToResponse(response, newTokens);
                processToken(newTokens.getAccessToken(), true, request);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void processToken(String token, boolean isAccessToken, HttpServletRequest request) {
        String email = jwtUtil.getEmail(token);
        List<String> roles = jwtUtil.getRoles(token);

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setRoles(roles);

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                customOAuth2User, null, customOAuth2User.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void addTokenToResponse(HttpServletResponse response, TokenResponse tokens) {
        // Access Token을 Authorization 헤더에 추가
        response.setHeader("Authorization", "Bearer " + tokens.getAccessToken());

        // Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = new Cookie("refresh_token", tokens.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS만 사용
        refreshTokenCookie.setPath("/api"); // API 경로에서만 사용 가능
        refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60); // 14일
        response.addCookie(refreshTokenCookie);

        // CORS 헤더 설정
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}