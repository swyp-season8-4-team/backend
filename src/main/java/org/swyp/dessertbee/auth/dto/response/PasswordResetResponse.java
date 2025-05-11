package org.swyp.dessertbee.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    private boolean success;
    private String message;

    public static PasswordResetResponse success() {
        return PasswordResetResponse.builder()
                .success(true)
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .build();
    }
}