package org.swyp.dessertbee.auth.service;

import org.swyp.dessertbee.auth.dto.TokenResponse;

/**
 * 인증 관련 서비스 인터페이스
 */
public interface AuthService {
    /**
     * 리프레시 토큰을 저장하거나 업데이트
     * @param email 사용자 이메일
     * @param refreshToken 리프레시 토큰
     */
    void saveRefreshToken(String email, String refreshToken);

    /**
     * 리프레시 토큰을 통해 새로운 액세스 토큰 발급
     * @param email 유저 이메일
     * @return 새로운 액세스 토큰 응답
     */
    TokenResponse refreshAccessToken(String email);

    /**
     * 리프레시 토큰 무효화 (로그아웃)
     * @param email 사용자 이메일
     */
    void revokeRefreshToken(String email);
}
