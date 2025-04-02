package org.swyp.dessertbee.auth.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.UUID;

/**
 * 로그인 응답을 위한 DTO 클래스
 * 로그인 성공 시 JWT 토큰과 사용자 정보를 담아 반환
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;     // JWT 액세스 토큰
    private String refreshToken;    // JWT 리프레시 토큰
    private String tokenType;       // 토큰 타입
    private long expiresIn;         // 토큰 만료 시간
    private UUID userUuid;          // 사용자 UUID
    private String email;           // 사용자 이메일
    private String nickname;        // 사용자 닉네임
    private String profileImageUrl; // 프로필 이미지
    private boolean isPreferenceSet; // 사용자 선호도 설정 여부
    private String deviceId;        // 디바이스 식별자

    /**
     * 로그인 성공 응답 생성 (디바이스 ID 포함)
     * @param accessToken JWT 액세스 토큰
     * @param refreshToken JWT 리프레시 토큰
     * @param expiresIn 액세스 토큰 만료 시간
     * @param user 사용자 엔티티
     * @param profileImageUrl 프로필 이미지 URL
     * @param deviceId 디바이스 식별자
     * @param isPreferenceSet 선호도 설정 여부
     * @return 로그인 응답 객체
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, UserEntity user, String profileImageUrl, String deviceId, boolean isPreferenceSet) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userUuid(user.getUserUuid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .deviceId(deviceId)
                .isPreferenceSet(isPreferenceSet)
                .build();
    }

    /**
     * 선호도 설정 제외하고 디바이스 ID 포함 응답 생성
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, UserEntity user, String profileImageUrl, String deviceId) {
        return success(accessToken, refreshToken, expiresIn, user, profileImageUrl, deviceId, false);
    }

    /**
     * 디바이스 ID 없이 선호도 설정 포함
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, UserEntity user, String profileImageUrl, boolean isPreferenceSet) {
        return success(accessToken, refreshToken, expiresIn, user, profileImageUrl, null, isPreferenceSet);
    }

    /**
     * 디바이스 ID, 선호도 설정 모두 없음
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, UserEntity user, String profileImageUrl) {
        return success(accessToken, refreshToken, expiresIn, user, profileImageUrl, null, false);
    }

}