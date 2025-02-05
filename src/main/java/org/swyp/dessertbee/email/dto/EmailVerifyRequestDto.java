package org.swyp.dessertbee.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerifyRequestDto {

    /**
     * 검증할 이메일 주소
     */
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    private String email;

    /**
     * 사용자가 입력한 인증 코드
     */
    @NotBlank(message = "인증 코드는 필수 입력값입니다.")
    @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
    private String code;

    /**
     * 이메일 검증 목적
     */
    @NotNull(message = "검증 목적은 필수 입력값입니다.")
    private EmailVerificationPurpose purpose;
}
