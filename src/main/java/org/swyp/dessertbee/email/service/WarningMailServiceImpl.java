package org.swyp.dessertbee.email.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarningMailServiceImpl implements WarningMailService {

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;
    private final String serviceName = "DesserBee";
    private final String senderName = "DesserBee 고객센터";

    /**
     * 경고 메일 발송
     * @param toEmail 수신자 이메일
     * @param reason 경고 사유
     */
    public void sendWarningEmail(String toEmail, String reason) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, senderName));
            helper.setTo(toEmail);
            helper.setSubject(serviceName + " 경고 안내");

            // Thymeleaf 템플릿 변수 설정
            Context context = new Context();
            context.setVariable("serviceName", serviceName);
            context.setVariable("reason", reason);

            // warning-email.html 템플릿 사용
            String htmlContent = templateEngine.process("warning-email", context);
            helper.setText(htmlContent, true);

            emailSender.send(message);
            log.debug("Warning email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("경고 이메일 발송 실패 - 이메일: {}", toEmail, e);
            // 필요에 따라 커스텀 예외 처리
            throw new BusinessException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }
}
