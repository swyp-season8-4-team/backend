package org.swyp.dessertbee.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "사용자 기본 정보 응답")
public class UserResponseDto {
    @Schema(
            description = "사용자 UUID",
            example = "8d06ef7a-7127-45e4-80c2-ab7eb5b74757",
            requiredMode = RequiredMode.REQUIRED
    )
    private String userUuid;  // 필수

    @Schema(
            description = "사용자 닉네임",
            example = "디저트비",
            requiredMode = RequiredMode.REQUIRED
    )
    private String nickname;  // 필수

    @Schema(
            description = "성별 (nullable)",
            example = "MALE",
            nullable = true,
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private UserEntity.Gender gender;    // 선택

    @Schema(
            description = "프로필 이미지 URL (nullable)",
            example = "https://desserbee-bucket.s3.ap-northeast-2.amazonaws.com/profile/56/d10eeb88-399c-4f6c-bd02-01ab7aa73eec-도너츠.jpeg",
            nullable = true,
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private String profileImageUrl;  // 선택

    @Schema(
            description = "선호도 ID 목록 (설정 안하면 빈배열)",
            example = "[1, 3, 5]",
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private List<Long> preferences;  // 선택

    @Schema(
            description = "MBTI 유형 (nullable)",
            example = "ENFP",
            nullable = true,
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private String mbti;     // 선택

    @Builder
    public UserResponseDto(String userUuid, String nickname, UserEntity.Gender gender,
                           String profileImageUrl, List<Long> preferences, String mbti) {
        this.userUuid = userUuid;
        this.nickname = nickname;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.preferences = preferences;
        this.mbti = mbti;
    }
}