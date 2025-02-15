package org.swyp.dessertbee.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.auth.dto.CustomOAuth2User;
import org.swyp.dessertbee.auth.dto.login.LoginResponse;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.service.AuthService;
import org.swyp.dessertbee.auth.service.TokenService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OAuth2 인증 성공 시 처리를 담당하는 핸들러
 * 인증 성공 후 JWT 토큰 생성 및 응답 처리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        log.info("OAuth2 로그인 성공 처리 시작");

        try {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            LoginResponse loginResponse = handleOAuth2Authentication(oauth2User);

            addTokenCookie(response, "accessToken", loginResponse.getAccessToken());
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:3030");

            // 응답 처리
            // writeLoginResponse(response, loginResponse);

        } catch (Exception e) {
            log.error("OAuth2 로그인 성공 처리 중 에러 발생", e);
            handleAuthenticationFailure(response, e);
        }
    }

    /**
     * OAuth2 인증 성공 후 JWT 토큰 생성 및 응답 데이터 생성
     */
    private LoginResponse handleOAuth2Authentication(CustomOAuth2User oauth2User) {
        String email = oauth2User.getEmail();
        UUID userUuid = oauth2User.getUserUuid();
        String nickname = oauth2User.getNickname();

        List<String> roles = oauth2User.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // JWT 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(email, roles, false);

        // 리프레시 토큰 생성 및 저장
        String refreshToken = jwtUtil.createRefreshToken(email, roles, false);
        tokenService.saveRefreshToken(email, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .userUuid(userUuid)
                .email(email)
                .nickname(nickname)
                .build();
    }

    /**
     * 로그인 성공 응답을 클라이언트에 전송
     */
    private void writeLoginResponse(HttpServletResponse response, LoginResponse loginResponse)
            throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String jsonResponse = objectMapper.writeValueAsString(loginResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 인증 실패 처리
     */
    private void handleAuthenticationFailure(HttpServletResponse response, Exception e)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Authentication failed");
        errorResponse.put("message", e.getMessage());

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .secure(false)  // localhost 개발 환경이므로 false
                .sameSite("Lax")  // localhost 개발 환경에서는 Lax 사용
                .httpOnly(true)
                .maxAge(Duration.ofHours(1))
                .domain("localhost")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}