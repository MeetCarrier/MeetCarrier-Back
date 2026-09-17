package com.kslj.mannam.domain.match.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class MatchScoringHttpConfig {

    @Bean
    public RestClient matchScoringRestClient(
            RestClient.Builder builder,
            @Value("${matching.scoring.base-url:http://localhost:8000}") String baseUrl,
            @Value("${matching.scoring.connect-timeout-ms:3000}") int connectTimeoutMillis,
            @Value("${matching.scoring.timeout-ms:10000}") int readTimeoutMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMillis);
        requestFactory.setReadTimeout(readTimeoutMillis);
        return builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
