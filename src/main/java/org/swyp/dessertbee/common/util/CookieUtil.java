package org.swyp.dessertbee.common.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * 쿠키 관련 유틸리티 클래스
 */
public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_PATH = "/api/auth/token/refresh";

    /**
     * 리프레시 토큰을 HTTP-only 쿠키로 설정
     *
     * @param response HTTP 응답 객체
     * @param refreshToken 리프레시 토큰
     * @param maxAge 쿠키 만료 시간 (초)
     */
    public static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)        // JavaScript에서 접근 불가능
                .secure(true)          // HTTPS 환경에서만 전송
                .sameSite("None")      // 크로스 사이트 요청 허용 (Strict에서 None으로 변경)
                .domain(".desserbee.com") // 모든 desserbee.com 서브도메인에서 쿠키 공유 가능하도록 설정
                .path(REFRESH_TOKEN_PATH) // 토큰 갱신 엔드포인트에서만 사용
                .maxAge(maxAge)        // 쿠키 만료 시간 (초)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 리프레시 토큰 쿠키 삭제
     *
     * @param response HTTP 응답 객체
     */
    public static void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")        // 크로스 사이트 요청 허용
                .domain(".desserbee.com") // 모든 desserbee.com 서브도메인에서 쿠키 공유 가능하도록 설정
                .path(REFRESH_TOKEN_PATH)
                .maxAge(0)  // 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}