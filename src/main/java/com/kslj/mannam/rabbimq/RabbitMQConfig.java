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

    // 비서 챗봇 통신용 큐
    @Bean
    public Queue aiResponseQueue() {
        return new Queue("ai_response_queue", true);
    }

    @Bean
    public Queue aiRequestQueue() {
        return new Queue("ai_request_queue", true);
    }

    // 매칭 큐
    @Bean
    public Queue matchRequestQueue() {
        return new Queue("match_request_queue", true);
    }

    @Bean
    public Queue matchResponseQueue() {
        return new Queue("match_response_queue", true);
    }

    // 개인 챗봇 통신용 큐
    @Bean
    public Queue chatbot_request_queue() {
        return new Queue("chatbot_request_queue", true);
    }

    @Bean
    public Queue chatbot_response_queue() {
        return new Queue("chatbot_response_queue", true);
    }
}
