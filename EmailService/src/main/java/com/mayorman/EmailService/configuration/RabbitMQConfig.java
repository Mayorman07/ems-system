package com.mayorman.EmailService.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. Define the "mailbox" (queue) for our service to listen to.
    @Bean
    Queue userCreatedEmailQueue() {
        // The queue is named "user-created-email-queue"
        // 'true' means the queue is durable (it won't be lost if RabbitMQ restarts)
        return new Queue("user-created-email-queue", true);
    }

    // 2. Define the "post office" (exchange) where messages are sent.
    // This name MUST match the one you created in the employees service.
    @Bean
    TopicExchange userEventsExchange() {
        return new TopicExchange("user-events-exchange");
    }

    // 3. Create a binding. This is the rule that connects our mailbox to the post office.
    // It says: "Send any message with the topic 'user.created' to my queue."
    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("user.created");
    }

    // --- ADD THIS NEW BEAN ---
    // This tells Spring to expect JSON messages and how to convert them.
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
