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

        if ("release".equals(activeProfile)) {
            Server server = new Server();
            server.setUrl("https://release.desserbee.com");
            server.setDescription("Release Server");
            openAPI.servers(List.of(server));
        }

        return openAPI;
    }

    // ✅ 전역 Platform-Type 헤더 설정 추가
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
