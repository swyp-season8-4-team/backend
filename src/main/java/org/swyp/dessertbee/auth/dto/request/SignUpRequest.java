package org.swyp.dessertbee.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.swyp.dessertbee.role.entity.RoleType;
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
    @Schema(
            description = "사용자 이메일",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;           // 사용자 이메일

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문자, 숫자, 특수문자를 포함해야 합니다."
    )
    @Schema(
            description = "비밀번호 (8자 이상, 영문자, 숫자, 특수문자 포함)",
            example = "Password1!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;        // 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
    @Schema(
            description = "비밀번호 확인 (비밀번호와 일치해야 함)",
            example = "Password1!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmPassword; // 비밀번호 확인

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    @Schema(
            description = "닉네임 (2자 이상 20자 이하)",
            example = "사용자1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nickname;        // 닉네임

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    @Schema(
            description = "이름 (2자 이상 50자 이하) (nullable)",
            example = "홍길동",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String name;            // 이름

    @Pattern(
            regexp = "^\\d{3}-\\d{3,4}-\\d{4}$",
            message = "올바른 전화번호 형식이 아닙니다."
    )
    @Schema(
            description = "전화번호 (nullable)",
            example = "010-1234-5678",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String phoneNumber;     // 전화번호

    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다.")
    @Schema(
            description = "주소 (nullable)",
            example = "서울시 강남구",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private String address;         // 주소

    @Schema(
            description = "성별 (MALE, FEMALE 중 하나) (nullable)",
            example = "MALE",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private UserEntity.Gender gender; // 성별

    @Schema(
            description = "선호도 ID 목록 (nullable)",
            example = "[1, 2, 3]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true
    )
    private List<Long> preferenceIds; // 선호도 ID 목록

    @Builder.Default
    @Schema(
            description = "사용자 역할 (기본값: ROLE_USER) (nullable)",
            example = "ROLE_USER",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private RoleType role = RoleType.ROLE_USER; // 기본값 ROLE_USER 설정

}