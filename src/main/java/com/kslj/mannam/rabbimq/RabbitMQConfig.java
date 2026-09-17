package com.kslj.mannam.rabbimq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

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

    // 순차 매칭 요청 큐
    @Bean
    public Queue matchAdmissionQueue() {
        return QueueBuilder.durable("match_admission_queue")
                .deadLetterExchange("")
                .deadLetterRoutingKey("match_admission_dlq")
                .build();
    }

    @Bean
    public Queue matchAdmissionDeadLetterQueue() {
        return QueueBuilder.durable("match_admission_dlq").build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory matchAdmissionListenerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter,
            RetryOperationsInterceptor matchAdmissionRetryInterceptor,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(matchAdmissionRetryInterceptor);
        factory.setAutoStartup(autoStartup);
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor matchAdmissionRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
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
