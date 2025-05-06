package com.kslj.mannam.swagger;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("만남배달부 API Document")
                .version("v0.0.2")
                .description("만남배달부 API 명세서입니다.");
        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
