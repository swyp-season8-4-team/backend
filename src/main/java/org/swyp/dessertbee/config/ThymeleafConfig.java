package org.swyp.dessertbee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * 이메일 템플릿 엔진 설정을 위한 Configuration
 */
@Configuration
public class ThymeleafConfig {

    /**
     * Thymeleaf 템플릿 엔진 Bean 생성
     * HTML 이메일 템플릿을 처리하기 위한 설정
     */
    @Bean
    public SpringTemplateEngine springTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }

    /**
     * HTML 템플릿 리졸버 설정
     * 템플릿 파일의 위치, 형식, 캐시 설정 등을 정의
     */
    @Bean
    public SpringResourceTemplateResolver htmlTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/mail/");  // 템플릿 파일 위치
        resolver.setSuffix(".html");  // 템플릿 파일 확장자
        resolver.setTemplateMode(TemplateMode.HTML);  // HTML 모드로 설정
        resolver.setCharacterEncoding("UTF-8");  // 인코딩 설정
        resolver.setCacheable(false);  // 개발 환경에서는 캐시 비활성화
        return resolver;
    }
}