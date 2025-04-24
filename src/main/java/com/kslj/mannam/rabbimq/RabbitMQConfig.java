package com.kslj.mannam.rabbimq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue aiResponseQueue() {
        return new Queue("ai_response_queue", true);
    }

    @Bean
    public Queue aiRequestQueue() {
        return new Queue("ai_request_queue", true);
    }
}
