package org.swyp.dessertbee.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

        // Access Token용 Security Scheme
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Access Token (Bearer 형식)");

        // Refresh Token용 Security Scheme
        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Refresh Token (Bearer 형식) - 토큰 재발급 전용");

        // 전역 설정
        SecurityRequirement defaultSecurity = new SecurityRequirement().addList("bearerAuth");

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addSecurityItem(defaultSecurity)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", accessTokenScheme)
                        .addSecuritySchemes("refreshTokenAuth", refreshTokenScheme));

        if ("release".equals(activeProfile)) {
            Server server = new Server();
            server.setUrl("https://release.desserbee.com");
            server.setDescription("Release Server");
            openAPI.servers(List.of(server));
        }

        return openAPI;
    }

    @Bean
    public OpenApiCustomizer platformTypeHeaderCustomizer() {
        return openApi -> {
            Parameter platformHeader = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema()._default("web"))
                    .name("Platform-Type")
                    .description("클라이언트 플랫폼 타입 (예: app 또는 web)")
                    .required(false);

            Paths paths = openApi.getPaths();
            if (paths != null) {
                paths.forEach((path, pathItem) -> {
                    pathItem.readOperations().forEach(operation -> {
                        operation.addParametersItem(platformHeader);
                    });
                });
            }
        };
    }
}