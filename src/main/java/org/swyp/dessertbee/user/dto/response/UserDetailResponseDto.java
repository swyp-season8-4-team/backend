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
@Schema(description = "사용자 상세 정보 응답")
public class UserDetailResponseDto extends UserResponseDto {
    @Schema(
            description = "사용자 이메일",
            example = "user@example.com",
            requiredMode = RequiredMode.REQUIRED
    )
    private String email;    // 필수

    @Schema(
            description = "사용자 이름 (nullable)",
            example = "홍길동",
            nullable = true,
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private String name;     // 선택

    @Schema(
            description = "전화번호 (nullable)",
            example = "010-1234-5678",
            nullable = true,
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private String phoneNumber;  // 선택

    @Schema(
            description = "주소 (nullable)",
            example = "서울시 강남구",
            nullable = true,
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    private String address;      // 선택

    @Schema(
            description = "취향 설정 여부 (해당 값을 통해 취향 설정 여부 판별, 사용자가 다음에 취향을 설정하겠다고 명시할 때를 위함.)",
            example = "true",
            requiredMode = RequiredMode.REQUIRED
    )
    private Boolean isPreferencesSet;  // 선호도 설정 여부

    @Schema(
            description = "사용자 역할 목록 (ROLE_USER/ROLE_OWNER)",
            example = "[\"ROLE_OWNER\"]",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<String> roles;


    @Builder(builderMethodName = "detailBuilder")
    public UserDetailResponseDto(String userUuid, String nickname, UserEntity.Gender gender,
                                 String profileImage, List<Long> preferences, String mbti,
                                 String email, String name, String phoneNumber, String address,
                                 Boolean isPreferencesSet, List<String> roles) {
        super(userUuid, nickname, gender, profileImage, preferences, mbti);
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.isPreferencesSet = isPreferencesSet;
        this.roles = roles;
    }
}