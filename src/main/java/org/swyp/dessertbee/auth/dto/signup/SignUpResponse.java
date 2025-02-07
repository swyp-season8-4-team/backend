package org.swyp.dessertbee.auth.dto.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpResponse {
    private String message;         // 결과 메시지
    private String email;           // 가입된 이메일


    public static SignUpResponse success(String email) {
        return SignUpResponse.builder()
                .message("회원가입이 완료되었습니다.")
                .email(email)
                .build();
    }
}