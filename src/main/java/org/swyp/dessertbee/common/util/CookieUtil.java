package org.swyp.dessertbee.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Optional;
import java.util.UUID;

/**
 * 쿠키 관련 유틸리티 클래스
 */
public class CookieUtil {

    private static final String DEVICE_ID_COOKIE_NAME = "deviceId";
    private static final String DEVICE_ID_PATH = "/";  // 디바이스 ID는 모든 경로에서 접근 가능하도록 설정

    /**
     * 디바이스 ID를 HTTP-only 쿠키로 설정
     *
     * @param response HTTP 응답 객체
     * @param deviceId 디바이스 ID
     * @param maxAge 쿠키 만료 시간 (초)
     */
    public static void addDeviceIdCookie(HttpServletResponse response, String deviceId, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(DEVICE_ID_COOKIE_NAME, deviceId)
                .httpOnly(true)        // JavaScript에서 접근 불가능
                .secure(true)          // HTTPS 환경에서만 전송
                .sameSite("None")      // 크로스 사이트 요청 허용
                .domain(".desserbee.com") // 모든 desserbee.com 서브도메인에서 쿠키 공유 가능하도록 설정
                .path(DEVICE_ID_PATH)   // 모든 경로에서 사용 가능
                .maxAge(maxAge)        // 쿠키 만료 시간 (초)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 디바이스 ID 쿠키 삭제
     *
     * @param response HTTP 응답 객체
     */
    public static void deleteDeviceIdCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(DEVICE_ID_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain(".desserbee.com")
                .path(DEVICE_ID_PATH)
                .maxAge(0)  // 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 요청에서 쿠키 가져오기
     *
     * @param request HTTP 요청
     * @param name 쿠키 이름
     * @return 쿠키 (Optional)
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * 새로운 디바이스 ID 생성
     *
     * @return 생성된 디바이스 ID
     */
    public static String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
}