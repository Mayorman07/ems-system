package com.mayorman.EmailService.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String USER_EVENTS_EXCHANGE = "user-events-exchange";
    public static final String USER_CREATED_QUEUE = "user-created-email-queue";
    public static final String PASSWORD_RESET_QUEUE = "password-reset-email-queue";

    @Bean
    TopicExchange userEventsExchange() {
        return new TopicExchange(USER_EVENTS_EXCHANGE);
    }

    // --- Queues ---

    // 1. Queue for user creation emails
    @Bean
    Queue userCreatedEmailQueue() {
        return new Queue(USER_CREATED_QUEUE, true);
    }
    @Bean
    Queue passwordResetEmailQueue() {
        return new Queue(PASSWORD_RESET_QUEUE, true); // durable = true
    }

    @Bean
    Binding userCreatedBinding(Queue userCreatedEmailQueue, TopicExchange userEventsExchange) {
        return BindingBuilder.bind(userCreatedEmailQueue).to(userEventsExchange).with("user.created");
    }
    @Bean
    Binding passwordResetBinding(Queue passwordResetEmailQueue, TopicExchange userEventsExchange) {
        // We assume the routing key will be "password.reset". Change if needed.
        return BindingBuilder.bind(passwordResetEmailQueue).to(userEventsExchange).with("password.reset");
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
