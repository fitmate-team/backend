package com.fitmate.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI fitMateOpenAPI() {
        String jwtSchemeName = "jwtAuth";

        SecurityRequirement securityRequirement
                = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName,
                                    new SecurityScheme()
                                            .name(jwtSchemeName)
                                            .type(SecurityScheme.Type.HTTP) // HTTP 방식
                                            .scheme("bearer")               // Bearer 토큰 형식
                                            .bearerFormat("JWT"));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(new Info()
                              .title("FitMate API")
                              .description("FitMate API 명세서")
                              .version("v1.0.0"));

    }
}
