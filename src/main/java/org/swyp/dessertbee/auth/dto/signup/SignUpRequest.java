package org.swyp.dessertbee.auth.dto.signup;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.user.entity.UserEntity;

import java.util.List;

/**
 * 회원가입 요청을 위한 DTO 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;           // 사용자 이메일

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;        // 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
    private String confirmPassword; // 비밀번호 확인

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String nickname;        // 닉네임

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;            // 이름

    @Pattern(
            regexp = "^\\d{3}-\\d{3,4}-\\d{4}$",
            message = "올바른 전화번호 형식이 아닙니다."
    )
    private String phoneNumber;     // 전화번호

    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다.")
    private String address;         // 주소

    private UserEntity.Gender gender; // 성별

    private List<Long> preferenceIds; // 선호도 ID 목록
}