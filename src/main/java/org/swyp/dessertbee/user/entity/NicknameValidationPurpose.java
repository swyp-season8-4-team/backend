package org.swyp.dessertbee.user.entity;

import lombok.Getter;

@Getter
public enum NicknameValidationPurpose {
    SIGNUP("회원가입"),
    PROFILE_UPDATE("프로필 수정");

    private final String description;

    NicknameValidationPurpose(String description) {
        this.description = description;
    }

}
