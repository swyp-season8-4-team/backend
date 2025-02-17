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
    private UUID userUuid;          // 사용자 UUID
    private String email;           // 사용자 이메일
    private String nickname;        // 사용자 닉네임
    private String profileImageUrl; // 프로필 이미지

    /**
     * 로그인 성공 응답 생성
     * @param token JWT 액세스 토큰
     * @param user 사용자 엔티티
     * @return 로그인 응답 객체
     */
    public static LoginResponse success(String token, UserEntity user, String profileImageUrl) {
        return LoginResponse.builder()
                .accessToken(token)
                .userUuid(user.getUserUuid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .build();
    }

    /**
     * 토큰만 포함된 응답 생성
     * 토큰 갱신 시 사용
     * @param token JWT 액세스 토큰
     * @return 로그인 응답 객체
     */
    public static LoginResponse withToken(String token) {
        return LoginResponse.builder()
                .accessToken(token)
                .build();
    }
}