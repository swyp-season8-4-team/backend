package org.swyp.dessertbee.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EmailVerifyResponseDto {

    /**
     * 이메일 검증 성공 여부
     */
    private boolean isVerified;

    /**
     * 검증 완료 시 발급되는 JWT 토큰 (30분 유효)
     */
    private String verificationToken;
}

