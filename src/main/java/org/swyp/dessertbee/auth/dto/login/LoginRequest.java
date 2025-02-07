package org.swyp.dessertbee.auth.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String email;           // 사용자 이메일
    private String password;        // 비밀번호
    private boolean keepLoggedIn;   // 로그인 상태 유지 여부
}