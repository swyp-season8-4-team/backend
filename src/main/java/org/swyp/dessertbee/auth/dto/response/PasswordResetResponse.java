package org.swyp.dessertbee.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    private boolean success;
    private String message;
    private int status;
    private LocalDateTime timestamp;

    public static PasswordResetResponse success() {
        return PasswordResetResponse.builder()
                .success(true)
                .message("비밀번호가 성공적으로 변경되었습니다.")
                .status(200)
                .timestamp(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }
}