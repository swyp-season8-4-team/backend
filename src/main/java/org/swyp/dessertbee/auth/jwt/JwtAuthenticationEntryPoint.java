package org.swyp.dessertbee.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.common.exception.ErrorResponse;

import java.io.IOException;

/**
 * JWT 인증 실패 처리를 위한 EntryPoint
 * 인증이 필요한 리소스에 인증 없이 접근하는 경우 처리
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        log.warn("인증 실패: {}", authException.getMessage());

        response.setStatus(ErrorCode.INVALID_CREDENTIALS.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        // ErrorResponse 사용
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ErrorCode.INVALID_CREDENTIALS.getHttpStatus().value())
                .code(ErrorCode.INVALID_CREDENTIALS.getCode())
                .message("인증이 필요합니다.")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
