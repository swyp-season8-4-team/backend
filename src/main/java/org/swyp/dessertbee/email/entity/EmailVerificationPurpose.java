package org.swyp.dessertbee.email.entity;

public enum EmailVerificationPurpose {
    SIGNUP("회원가입"),
    PASSWORD_RESET("비밀번호 재설정");

    private final String description;

    EmailVerificationPurpose(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
