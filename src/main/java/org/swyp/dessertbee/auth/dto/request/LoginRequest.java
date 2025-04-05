package org.swyp.dessertbee.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    @Schema(description = "사용자 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;           // 사용자 이메일

    @NotBlank(message = "비밀번호는 필수 입력값입니다")
    @Schema(description = "비밀번호", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;        // 비밀번호

    @Schema(description = "로그인 상태 유지 여부", defaultValue = "false", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean keepLoggedIn;   // 로그인 상태 유지 여부
}