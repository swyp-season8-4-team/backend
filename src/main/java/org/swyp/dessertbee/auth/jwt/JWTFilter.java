package org.swyp.dessertbee.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.dto.UserOAuthDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractTokenFromHeader(request);

            if (token != null) {
                if (jwtUtil.validateToken(token, true)) {
                    Authentication authentication = createAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendAuthenticationError(response, ErrorCode.EXPIRED_VERIFICATION_TOKEN, "만료된 인증 토큰입니다.");
        } catch (JwtException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendAuthenticationError(response, ErrorCode.INVALID_VERIFICATION_TOKEN, "유효하지 않은 인증 토큰입니다.");
        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생", e);
            SecurityContextHolder.clearContext();
            sendAuthenticationError(response, ErrorCode.INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private void sendAuthenticationError(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", errorCode.getHttpStatus().value());
        errorResponse.put("code", errorCode.getCode());
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }



    /**
     * Request Header에서 토큰 추출
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        // 대소문자 구분 없이 모든 헤더를 확인
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER.toLowerCase());
        if (bearerToken == null) {
            // 첫 번째 시도가 실패하면 원래 대문자 버전으로 시도
            bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        }

        if (StringUtils.hasText(bearerToken) &&
                bearerToken.toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
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