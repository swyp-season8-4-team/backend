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

    /**
     * 로그인 성공 응답 생성
     * @param accessToken JWT 액세스 토큰
     * @param expiresIn 액세스 토큰 만료 시간 (파라미터 추가됨)
     * @param user 사용자 엔티티
     * @return 로그인 응답 객체
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, UserEntity user, String profileImageUrl, boolean isPreferenceSet) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer") // 토큰 타입 설정 (추가됨)
                .expiresIn(expiresIn) // 만료 시간 설정 (추가됨)
                .userUuid(user.getUserUuid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .isPreferenceSet(isPreferenceSet) // 선호도 설정 여부
                .build();
    }

    /**
     * 회원가입을 위한 오버로딩 메서드
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, UserEntity user, String profileImageUrl) {
        return success(accessToken, refreshToken, expiresIn, user, profileImageUrl, false);
    }
}