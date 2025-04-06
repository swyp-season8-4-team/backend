package org.swyp.dessertbee.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "닉네임 검증 용도")
public enum NicknameValidationPurpose {
    @Schema(description = "회원가입 시 닉네임 검증")
    SIGNUP("회원가입"),

    @Schema(description = "프로필 수정 시 닉네임 검증")
    PROFILE_UPDATE("프로필 수정");

    private final String description;

    NicknameValidationPurpose(String description) {
        this.description = description;
    }
}
