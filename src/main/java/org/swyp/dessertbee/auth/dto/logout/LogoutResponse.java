package org.swyp.dessertbee.auth.dto.logout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {
    private String message;

    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .message("로그아웃이 완료되었습니다.")
                .build();
    }
}