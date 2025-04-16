package org.swyp.dessertbee.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "사용자 정보 업데이트 요청")
public class UserUpdateRequestDto {
    @Schema(
            description = "사용자 닉네임",
            example = "디저트비",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String nickname;

    @Schema(
            description = "선호도 ID 목록",
            example = "[1, 3, 5]",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<Long> preferences;

    @Schema(
            description = "사용자 이름",
            example = "홍길동",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name;

    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$|^01(?:0|1|[6-9])-\\d{4}-\\d{4}$",
            message = "올바른 휴대폰 번호 형식이 아닙니다.")
    @Schema(
            description = "휴대폰 번호 (형식: 010-1234-5678 또는 01012345678)",
            example = "010-1234-5678",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String phoneNumber;

    @Schema(
            description = "주소",
            example = "서울시 강남구 역삼동 123-45",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String address;

    @Schema(
            description = "성별 (MALE/FEMALE)",
            example = "MALE",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private UserEntity.Gender gender;

    @Schema(
            description = "MBTI 유형",
            example = "ENFP",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String mbti;

    @Schema(
            description = "사용자 역할 목록 (ROLE_USER/ROLE_OWNER)",
            example = "[\"ROLE_OWNER\"]",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> roles;


    @Builder
    public UserUpdateRequestDto(String nickname, List<Long> preferences, String name,
                                String phoneNumber, String address, UserEntity.Gender gender,
                                String mbti, List<String> roles) {
        this.nickname = nickname;
        this.preferences = preferences;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.gender = gender;
        this.mbti = mbti;
        this.roles = roles;
    }
}