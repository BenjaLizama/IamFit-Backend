package com.iamfit.ai_service.configuration;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${iamfit.rabbitmq.queues.user-created}")
    private String userCreatedQueue;

    // ===== EXCHANGE =====
    @Bean
    public TopicExchange iamfitExchange() {
        return new TopicExchange("iamfit.exchange", true, false);
    }

    // ===== COLAS =====
    @Bean
    public Queue userCreatedQueue() {
        // durable=true → la cola sobrevive reinicios del broker
        return new Queue(userCreatedQueue, true);
    }

    // ===== BINDINGS =====
    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange iamfitExchange) {
        return BindingBuilder
                .bind(userCreatedQueue)
                .to(iamfitExchange)
                .with("iamfit.user.created");
    }

    // ===== CONVERTER JSON =====
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}