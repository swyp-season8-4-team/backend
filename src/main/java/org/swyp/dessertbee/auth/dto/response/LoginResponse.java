package org.swyp.dessertbee.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(
            description = "JWT 액세스 토큰",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accessToken;     // JWT 액세스 토큰
    @Schema(
            description = "JWT 리프레시 토큰",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;    // JWT 리프레시 토큰
    @Schema(
            description = "토큰 타입 (항상 'Bearer')",
            example = "Bearer",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tokenType;       // 토큰 타입
    @Schema(
            description = "토큰 만료 시간 (밀리초)",
            example = "259200000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long expiresIn;         // 토큰 만료 시간
    @Schema(
            description = "사용자 UUID",
            example = "8d06ef7a-7127-45e4-80c2-ab7eb5b74757",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private UUID userUuid;          // 사용자 UUID
    @Schema(
            description = "사용자 이메일",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;           // 사용자 이메일
    @Schema(
            description = "사용자 닉네임",
            example = "userTester1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nickname;        // 사용자 닉네임
    @Schema(
            description = "프로필 이미지 URL (nullable)",
            nullable = true,
            example = "https://example.com/profile.jpg",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String profileImageUrl; // 프로필 이미지
    @Schema(
            description = "사용자 선호도 설정 여부",
            example = "false",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean isPreferenceSet; // 사용자 선호도 설정 여부
    @Schema(
            description = "디바이스 식별자(없다면 서버에서 생성후 발급)",
            example = "5fa946a8-3374-4df3-8800-f869eb070c07",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
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