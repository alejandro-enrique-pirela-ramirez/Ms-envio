package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "facturador.envio.exchange";
    public static final String QUEUE_ENVIAR = "facturador.envio.enviar.queue";
    public static final String RK_ENVIAR = "factura.enviar";

    @Bean
    public DirectExchange envioExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue envioEnviarQueue() {
        return new Queue(QUEUE_ENVIAR, true);
    }

    @Bean
    public Binding envioEnviarBinding(Queue envioEnviarQueue, DirectExchange envioExchange) {
        return BindingBuilder.bind(envioEnviarQueue).to(envioExchange).with(RK_ENVIAR);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}