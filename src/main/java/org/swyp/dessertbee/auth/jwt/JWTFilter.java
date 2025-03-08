package org.swyp.dessertbee.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.swyp.dessertbee.auth.dto.userdetails.CustomUserDetails;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.exception.ErrorResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
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

        log.debug("요청 시작: {}", request.getRequestURI());

        String token = extractTokenFromHeader(request);
        log.debug("추출된 토큰: {}", token);

        if (token != null) {
            ErrorCode errorCode = jwtUtil.validateToken(token, true);

            if (errorCode == null) {
                // 토큰이 유효한 경우 인증 처리
                Authentication authentication = createAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("인증 성공: '{}'", maskToken(token));
                log.debug("SecurityContext에 저장된 Authentication: {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                // 토큰 검증 실패 - 구체적인 오류 코드로 응답
                log.warn("JWT 검증 실패: {}", errorCode);
                SecurityContextHolder.clearContext();
                handleJwtException(response, errorCode);
                return; // 필터 체인 중단
            }
        }
        filterChain.doFilter(request, response);
        log.debug("필터 체인 실행 후 SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());
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
        List<String> roleNames = jwtUtil.getRoles(token, true);
        UUID userUuid = jwtUtil.getUserUuid(token, true);

        // DB 조회 없이 CustomUserDetails 객체를 생성
        CustomUserDetails userDetails = new CustomUserDetails(roleNames, userUuid);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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

    /**
     * JWT 예외 처리 및 응답 생성
     */
    private void handleJwtException(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        // JavaTimeModule 등록
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}