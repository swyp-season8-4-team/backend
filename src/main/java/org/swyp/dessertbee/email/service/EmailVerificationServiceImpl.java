package org.swyp.dessertbee.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.email.dto.EmailVerificationRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerificationResponseDto;
import org.swyp.dessertbee.email.dto.EmailVerifyRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerifyResponseDto;
import org.swyp.dessertbee.email.entity.EmailVerificationEntity;
import org.swyp.dessertbee.email.repository.EmailVerificationRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender emailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JWTUtil jwtUtil;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 인증 코드 만료 시간 (분)
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    @Override
    @Transactional
    public EmailVerificationResponseDto sendVerificationEmail(EmailVerificationRequestDto request) {
        String verificationCode = generateVerificationCode();

        // 이메일 인증 정보 저장
        EmailVerificationEntity verification = EmailVerificationEntity.builder()
                .email(request.getEmail())
                .code(verificationCode)
                .purpose(request.getPurpose())
                .verified(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        emailVerificationRepository.save(verification);

        // 이메일 발송
        sendEmail(request.getEmail(), verificationCode);

        return EmailVerificationResponseDto.builder()
                .message("인증 코드가 발송되었습니다.")
                .expirationMinutes(5)
                .build();
    }

    @Override
    @Transactional
    public EmailVerifyResponseDto verifyEmail(EmailVerifyRequestDto request) {
        // 이메일 검증 정보 조회
        EmailVerificationEntity verification = emailVerificationRepository
                .findByEmailAndCodeAndPurpose(request.getEmail(), request.getCode(), request.getPurpose())
                .orElseThrow(() -> new InvalidVerificationException("유효하지 않은 인증 코드입니다."));

        // 만료 여부 확인
        if (verification.isExpired()) {
            throw new InvalidVerificationException("만료된 인증 코드입니다.");
        }

        // 이미 검증된 코드인지 확인
        if (verification.isVerified()) {
            throw new InvalidVerificationException("이미 사용된 인증 코드입니다.");
        }

        // 검증 완료 처리
        verification.verify();

        // 인증 토큰 생성
        String verificationToken = jwtUtil.createEmailVerificationToken(
                request.getEmail(),
                request.getPurpose()
        );

        return EmailVerifyResponseDto.builder()
                .isVerified(true)
                .verificationToken(verificationToken)
                .build();
    }


    /**
     * 6자리 랜덤 인증 코드 생성
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 인증 이메일 발송
     */
    private void sendEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n" +
                "유효시간은 5분입니다.");

        emailSender.send(message);
    }

    public static class InvalidVerificationException extends RuntimeException {
        public InvalidVerificationException(String message) {
            super(message);
        }
    }
}