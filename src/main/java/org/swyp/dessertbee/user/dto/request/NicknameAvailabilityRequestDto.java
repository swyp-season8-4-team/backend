package org.swyp.dessertbee.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.swyp.dessertbee.user.entity.NicknameValidationPurpose;

@Getter
@Schema(description = "닉네임 중복 검사 요청")
public class NicknameAvailabilityRequestDto {
    @NotNull(message = "닉네임은 필수 입력값입니다.")
    @Schema(
            description = "검사할 닉네임",
            example = "디저트비",
            requiredMode = RequiredMode.REQUIRED
    )
    private final String nickname;

    @NotNull(message = "용도는 필수 입력값입니다.")
    @Schema(
            description = "검사 용도 (SIGNUP: 회원가입, PROFILE_UPDATE: 프로필 수정)",
            example = "SIGNUP",
            requiredMode = RequiredMode.REQUIRED
    )
    private final NicknameValidationPurpose purpose;

    @Builder
    public NicknameAvailabilityRequestDto(String nickname, NicknameValidationPurpose purpose) {
        this.nickname = nickname;
        this.purpose = purpose;
    }
}