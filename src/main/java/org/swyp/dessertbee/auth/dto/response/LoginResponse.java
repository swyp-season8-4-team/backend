package org.swyp.dessertbee.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.common.exception.ErrorResponse;
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

    @Schema(description = "리프레시 토큰 만료 시간 (밀리초)", example = "864000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private long refreshExpiresIn;

    @Schema(
            description = "액세스 토큰 만료 시간 (밀리초)",
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

    @Schema(
            description = "앱에서 로그인한 요청인지 여부",
            example = "false",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Builder.Default
    private Boolean fromApp = false; // 앱에서 로그인 여부

    @Schema(
            description = "OAuth 계정 자동 연결 발생 여부",
            example = "true",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Builder.Default
    private Boolean accountLinkingOccurred = false; // OAuth 계정 자동 연결 여부

    @Schema(
            description = "연결된 OAuth 제공자 목록",
            example = "[\"apple\", \"kakao\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private java.util.List<String> linkedProviders; // 연결된 OAuth 제공자 목록

    @Schema(
            description = "이미지 관련 오류 메시지 (이미지 업로드 실패 시에만 존재)",
            implementation = ErrorResponse.class,
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = """
                {
                  "status": 400,
                  "code": "F004",
                  "message": "지원하지 않는 파일 형식입니다.",
                  "timestamp": "2025-04-15T02:45:12"
                }
            """
    )
    private ErrorResponse imageError;     // 이미지 오류 정보

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
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn,
                                        UserEntity user, String profileImageUrl, String deviceId, boolean isPreferenceSet) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .userUuid(user.getUserUuid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .deviceId(deviceId)
                .isPreferenceSet(isPreferenceSet)
                .fromApp(false)
                .accountLinkingOccurred(false)
                .build();
    }

    /**
     * 로그인 성공 응답 생성 (계정 연결 정보 포함)
     * @param accessToken JWT 액세스 토큰
     * @param refreshToken JWT 리프레시 토큰
     * @param expiresIn 액세스 토큰 만료 시간
     * @param user 사용자 엔티티
     * @param profileImageUrl 프로필 이미지 URL
     * @param deviceId 디바이스 식별자
     * @param isPreferenceSet 선호도 설정 여부
     * @param accountLinkingOccurred 계정 연결 발생 여부
     * @param linkedProviders 연결된 OAuth 제공자 목록
     * @return 로그인 응답 객체
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn,
                                        UserEntity user, String profileImageUrl, String deviceId, boolean isPreferenceSet,
                                        boolean accountLinkingOccurred, java.util.List<String> linkedProviders) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .refreshExpiresIn(refreshExpiresIn)
                .userUuid(user.getUserUuid())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(profileImageUrl)
                .deviceId(deviceId)
                .isPreferenceSet(isPreferenceSet)
                .fromApp(false)
                .accountLinkingOccurred(accountLinkingOccurred)
                .linkedProviders(linkedProviders)
                .build();
    }

    /**
     * 선호도 설정 제외하고 디바이스 ID 포함 응답 생성
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn,
                                        UserEntity user, String profileImageUrl, String deviceId) {
        return success(accessToken, refreshToken, expiresIn, refreshExpiresIn, user, profileImageUrl, deviceId, false);
    }

    /**
     * 디바이스 ID 없이 선호도 설정 포함
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn,
                                        UserEntity user, String profileImageUrl, boolean isPreferenceSet) {
        return success(accessToken, refreshToken, expiresIn, refreshExpiresIn, user, profileImageUrl, null, isPreferenceSet);
    }

    /**
     * 디바이스 ID, 선호도 설정 모두 없음
     */
    public static LoginResponse success(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn,
                                        UserEntity user, String profileImageUrl) {
        return success(accessToken, refreshToken, expiresIn, refreshExpiresIn, user, profileImageUrl, null, false);
    }
}