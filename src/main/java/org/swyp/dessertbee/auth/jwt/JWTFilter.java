package org.swyp.dessertbee.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.user.dto.UserOAuthDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 토큰을 검증하고 인증을 처리하는 필터
 * Spring Security Filter Chain에서 사용됨
 */
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromHeader(request);

            if (token != null && jwtUtil.validateToken(token, true)) {
                // 토큰이 유효한 경우 인증 처리
                Authentication authentication = createAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set Authentication to security context for '{}' token",
                        maskToken(token));
            }

        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
            // 만료된 토큰 처리는 프론트엔드에서 처리하도록 함
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 토큰 추출
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * JWT 토큰으로부터 인증 객체 생성
     */
    private Authentication createAuthentication(String token) {
        String email = jwtUtil.getEmail(token, true);
        List<String> roles = jwtUtil.getRoles(token, true);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        UserOAuthDto userOAuthDto = UserOAuthDto.builder()
                .email(email)
                .roles(roles)
                .build();

        CustomOAuth2User principal = new CustomOAuth2User(userOAuthDto, new HashMap<>());

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    /**
     * 토큰 마스킹 처리 (로깅용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." +
                token.substring(token.length() - 4);
    }

    /**
     * 특정 요청 경로에 대해 필터 스킵
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // 인증이 필요없는 경로 설정
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/oauth2/") ||
                path.startsWith("/api/public/");
    }
}