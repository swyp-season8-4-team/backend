package org.swyp.dessertbee.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 이메일 설정을 위한 Configuration 클래스
 * JavaMailSender를 설정하고 SMTP 속성을 정의
 */
@Configuration
public class EmailConfig {

    /**
     * JavaMailSender Bean 설정
     * @param host SMTP 서버 호스트
     * @param port SMTP 서버 포트
     * @param username 이메일 계정
     * @param password 앱 비밀번호
     * @return 설정된 JavaMailSender 인스턴스
     */
    @Bean
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        // SMTP 프로토콜 설정
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");  // 디버깅 활성화

        return mailSender;
    }
}