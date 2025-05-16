package com.kslj.mannam.swagger;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("만남배달부 API Document")
                .version("v0.0.3")
                .description("만남배달부 API 명세서입니다.");
        Server server = new Server();
        server.setUrl("https://www.mannamdeliveries.link");

        return new OpenAPI()
                .servers(List.of(server))
                .components(new Components())
                .info(info);
    }
}
