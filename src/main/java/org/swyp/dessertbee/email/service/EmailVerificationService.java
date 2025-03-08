package org.swyp.dessertbee.email.service;

import org.swyp.dessertbee.email.dto.EmailVerificationRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerificationResponseDto;
import org.swyp.dessertbee.email.dto.EmailVerifyRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerifyResponseDto;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;

public interface EmailVerificationService {
    /**
     * 이메일 인증 코드 생성 및 발송
     */
    EmailVerificationResponseDto sendVerificationEmail(EmailVerificationRequestDto request);

    /**
     * 이메일 인증 코드 확인
     */
    EmailVerifyResponseDto verifyEmail(EmailVerifyRequestDto request);

    /**
     * 이메일 인증 토큰 검증
     */
    void validateEmailVerificationToken(String token, String email, EmailVerificationPurpose purpose);

}
