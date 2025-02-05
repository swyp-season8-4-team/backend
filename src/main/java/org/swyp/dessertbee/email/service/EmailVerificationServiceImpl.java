package org.swyp.dessertbee.email.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.email.dto.EmailVerificationRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerificationResponseDto;
import org.swyp.dessertbee.email.dto.EmailVerifyRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerifyResponseDto;
import org.swyp.dessertbee.email.entity.EmailVerificationEntity;
import org.swyp.dessertbee.email.repository.EmailVerificationRepository;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender emailSender;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JWTUtil jwtUtil;
    private final SpringTemplateEngine templateEngine;


    @Value("${spring.mail.username}")
    private String fromEmail;
    private final String serviceName = "DesserBee";
    private final String senderName = "DesserBee 고객센터";

    // 인증 코드 만료 시간 (분)
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    @Override
    @Transactional
    public EmailVerificationResponseDto sendVerificationEmail(EmailVerificationRequestDto request) {

        // 최근 30분간 요청 횟수 체크
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        long recentRequests = emailVerificationRepository
                .countRecentVerificationRequests(request.getEmail(), thirtyMinutesAgo);

        if (recentRequests >= 10) {
            throw new InvalidVerificationException("너무 많은 인증 요청이 있었습니다. 잠시 후 다시 시도해주세요.");
        }

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
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 발신자 정보 설정
            helper.setFrom(new InternetAddress(fromEmail, senderName));
            helper.setTo(toEmail);
            helper.setSubject(serviceName + " 이메일 인증");

            // Thymeleaf 컨텍스트 설정
            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("serviceName", serviceName);
            context.setVariable("expirationMinutes", VERIFICATION_CODE_EXPIRY_MINUTES);

            // HTML 템플릿 처리
            String htmlContent = templateEngine.process("verification-email", context);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.debug("Verification email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    public static class InvalidVerificationException extends RuntimeException {
        public InvalidVerificationException(String message) {
            super(message);
        }
    }

    /**
     * 배치 작업으로 오래된 데이터 정리
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupOldVerifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);  // 7일 이상 지난 데이터
        emailVerificationRepository.softDeleteOldRecords(threshold);
        log.info("Cleaned up old email verification records before {}", threshold);
    }
}