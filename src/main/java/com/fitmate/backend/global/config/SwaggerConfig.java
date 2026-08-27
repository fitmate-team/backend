package com.fitmate.backend.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI nyArchiveOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("FitMate API")
                              .description("FitMate API 명세서")
                              .version("v1.0.0"));
    }
}
