package org.swyp.dessertbee.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${SPRING_PROFILES_ACTIVE:unknown}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("DessertBee API")
                .version("v1.0")
                .description("DessertBee 서비스의 API 문서");

        // JWT 인증 스키마 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));

        // 프로덕션 환경에서만 서버 URL 설정 추가
        if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            Server server = new Server();
            server.setUrl("https://api.desserbee.com");
            server.setDescription("Production Server");
            openAPI.servers(List.of(server));
        }

        return openAPI;
    }
}