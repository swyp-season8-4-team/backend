package org.swyp.dessertbee.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.swyp.dessertbee.user.entity.NicknameValidationPurpose;

@Getter
public class NicknameValidationRequestDto {
    @NotNull(message = "닉네임은 필수 입력값입니다.")
    private final String nickname;

    @NotNull(message = "용도는 필수 입력값입니다.")
    private final NicknameValidationPurpose purpose;

    @Builder
    public NicknameValidationRequestDto(String nickname, NicknameValidationPurpose purpose) {
        this.nickname = nickname;
        this.purpose = purpose;
    }
}
