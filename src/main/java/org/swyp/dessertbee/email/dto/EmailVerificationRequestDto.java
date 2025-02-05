package org.swyp.dessertbee.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

/**
 * 이메일 검증 요청을 위한 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationRequestDto {

    /**
     * 검증할 이메일 주소
     */
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    /**
     * 이메일 검증 목적 (SIGNUP 또는 PASSWORD_RESET)
     */
    @NotNull(message = "검증 목적은 필수 입력값입니다.")
    private EmailVerificationPurpose purpose;
}
