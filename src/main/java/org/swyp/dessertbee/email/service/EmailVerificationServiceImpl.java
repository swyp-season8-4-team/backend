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
import java.time.temporal.ChronoUnit;
import java.util.List;
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
                validateDeletedAccount(request.getEmail());
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

            // 기존 인증 코드 만료 처리
            expireExistingVerifications(request.getEmail(), request.getPurpose());

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
                throw new BusinessException(ErrorCode.EXPIRED_EMAIL_VERIFICATION_CODE, "만료된 인증 코드입니다.");
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
                    verification.getId(),  // 인증 ID 전달
                    request.getPurpose()   // 인증 목적 전달
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

    /**
     * 탈퇴한 계정의 재가입 제한 기간을 검증합니다.
     * @param email 가입하려는 이메일
     * @throws BusinessException 재가입 제한 기간인 경우
     */
    private void validateDeletedAccount(String email) {
        userRepository.findDeletedAccountByEmail(email).ifPresent(deletedUser -> {
            LocalDateTime deletedAt = deletedUser.getDeletedAt();
            LocalDateTime restrictedUntil = deletedAt.plusMonths(1);

            if (LocalDateTime.now().isBefore(restrictedUntil)) {
                long daysUntilAvailable = ChronoUnit.DAYS.between(
                        LocalDateTime.now(),
                        restrictedUntil
                );
                throw new BusinessException(
                        ErrorCode.SIGNUP_RESTRICTED_DELETED_ACCOUNT,
                        String.format("탈퇴한 계정은 %d일 후에 재가입이 가능합니다.", daysUntilAvailable)
                );
            }
        });
    }

    /**
     * 기존 인증 코드들을 만료 처리합니다.
     */
    private void expireExistingVerifications(String email, EmailVerificationPurpose purpose) {
        List<EmailVerificationEntity> existingVerifications = emailVerificationRepository
                .findByEmailAndPurposeAndVerifiedFalseAndDeletedAtIsNull(email, purpose);

        if (!existingVerifications.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            existingVerifications.forEach(verification -> {
                verification.updateExpiresAt(now);
                verification.updateDeletedAt(now);
            });
            emailVerificationRepository.saveAll(existingVerifications);
            log.debug("기존 인증 코드 만료 처리 완료 - 이메일: {}, 처리 건수: {}", email, existingVerifications.size());
        }
    }

    /**
     * 이메일 인증 토큰 검증
    */
     @Override
    @Transactional(readOnly = true)
    public void validateEmailVerificationToken(String token, String email, EmailVerificationPurpose purpose) {
        try {
            // 토큰 유효성 검사
            if (jwtUtil.validateToken(token, true) != null) {
                log.warn("토큰 검증 실패 - 만료되거나 유효하지 않은 인증 토큰: {}", token);
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "만료되었거나 유효하지 않은 이메일 인증 토큰입니다.");
            }

            // 토큰에서 인증 ID 추출
            Long verificationId = jwtUtil.getVerificationId(token);

            // 인증 정보 조회
            EmailVerificationEntity verification = emailVerificationRepository
                    .findById(verificationId)
                    .orElseThrow(() -> {
                        log.warn("토큰 검증 실패 - 존재하지 않는 인증 ID: {}", verificationId);
                        return new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "유효하지 않은 인증 토큰입니다.");
                    });

            // 이메일 일치 여부 확인
            if (!verification.getEmail().equals(email)) {
                log.warn("토큰 검증 실패 - 저장된 이메일({})과 요청한 이메일({}) 불일치",
                        verification.getEmail(), email);
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "인증된 이메일과 요청한 이메일이 일치하지 않습니다.");
            }

            // 인증 목적 확인
            if (verification.getPurpose() != purpose) {
                log.warn("토큰 검증 실패 - 예상된 목적({})과 인증의 목적({}) 불일치",
                        purpose, verification.getPurpose());
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "유효하지 않은 인증 토큰입니다.");
            }

            // 인증 확인
            if (!verification.isVerified()) {
                log.warn("토큰 검증 실패 - 인증되지 않은 이메일: {}", verification.getEmail());
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_TOKEN, "이메일 인증이 완료되지 않았습니다.");
            }

            // 검증 성공 로그 추가
            log.info("토큰 검증 성공 - 이메일: {}, 용도: {}", verification.getEmail(), verification.getPurpose());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("이메일 인증 토큰 검증 중 알 수 없는 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}