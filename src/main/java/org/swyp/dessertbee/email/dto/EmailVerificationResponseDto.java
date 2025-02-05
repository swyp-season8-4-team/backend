package org.swyp.dessertbee.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 검증 요청에 대한 응답 DTO
 */
@Getter
@AllArgsConstructor
@Builder
public class EmailVerificationResponseDto {

    /**
     * 사용자에게 보여줄 메시지
     */
    private String message;

    /**
     * 인증 코드 만료 시간 (분)
     */
    private int expirationMinutes;
}