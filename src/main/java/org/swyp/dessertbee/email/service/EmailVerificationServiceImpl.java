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
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.email.dto.EmailVerificationRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerificationResponseDto;
import org.swyp.dessertbee.email.dto.EmailVerifyRequestDto;
import org.swyp.dessertbee.email.dto.EmailVerifyResponseDto;
import org.swyp.dessertbee.email.entity.EmailVerificationEntity;
import org.swyp.dessertbee.email.entity.EmailVerificationPurpose;
import org.swyp.dessertbee.email.repository.EmailVerificationRepository;
import org.swyp.dessertbee.user.repository.UserRepository;
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
    private final UserRepository userRepository;



    @Value("${spring.mail.username}")
    private String fromEmail;
    private final String serviceName = "DesserBee";
    private final String senderName = "DesserBee 고객센터";

    // 인증 코드 만료 시간 (분)
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 5;

    @Override
    @Transactional
    public EmailVerificationResponseDto sendVerificationEmail(EmailVerificationRequestDto request) {

        try {

            if (request.getPurpose() == EmailVerificationPurpose.SIGNUP) {
                boolean isRegistered = userRepository.existsByEmail(request.getEmail());
                if (isRegistered) {
                    log.warn("이메일 인증 요청 실패 - 이미 가입된 이메일: {}", request.getEmail());
                    throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일입니다.");
                }
            }

            // 최근 30분간 요청 횟수 체크
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
            long recentRequests = emailVerificationRepository
                    .countRecentVerificationRequests(request.getEmail(), thirtyMinutesAgo);

            if (recentRequests >= 10) {
                log.warn("이메일 인증 요청 과다 - 이메일: {}", request.getEmail());
                throw new BusinessException(ErrorCode.TOO_MANY_VERIFICATION_REQUESTS);
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
        } catch (BusinessException e) {
                log.warn("이메일 인증 요청 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
                throw e;
            } catch(Exception e){
                log.error("이메일 인증 처리 중 오류 발생 - 이메일: {}", request.getEmail(), e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
    }

    @Override
    @Transactional
    public EmailVerifyResponseDto verifyEmail(EmailVerifyRequestDto request) {
        try {
            // 이메일 검증 정보 조회
            EmailVerificationEntity verification = emailVerificationRepository
                    .findByEmailAndCodeAndPurpose(request.getEmail(), request.getCode(), request.getPurpose())
                    .orElseThrow(() -> {
                        log.warn("이메일 인증 실패 - 유효하지 않은 코드, 이메일: {}", request.getEmail());
                        return new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN);
                    });

            // 만료 여부 확인
            if (verification.isExpired()) {
                log.warn("이메일 인증 실패 - 만료된 인증 코드, 이메일: {}", request.getEmail());
                throw new BusinessException(ErrorCode.EXPIRED_VERIFICATION_TOKEN, "만료된 인증 코드입니다.");
            }

            // 이미 검증된 코드인지 확인
            if (verification.isVerified()) {
                log.warn("이메일 인증 실패 - 이미 사용된 코드, 이메일: {}", request.getEmail());
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "이미 사용된 인증 코드입니다.");
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
        } catch (BusinessException e) {
            log.warn("이메일 인증 실패 - 이메일: {}, 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("이메일 인증 중 알 수 없는 오류 발생 - 이메일: {}", request.getEmail(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 인증 처리 중 오류가 발생했습니다.");
        }
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
            log.error("이메일 발송 실패 - 이메일: {}", toEmail, e);
            throw new BusinessException(ErrorCode.EMAIL_SENDING_FAILED);
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