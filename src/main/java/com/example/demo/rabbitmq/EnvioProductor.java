package com.example.demo.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.bean.dto.EnvioRequestDto;
import com.example.demo.bean.event.EnvioFacturaEvent;
import com.example.demo.config.RabbitMqConfig;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnvioProductor {

    private final RabbitTemplate rabbitTemplate;

    public EnvioProductor(RabbitTemplate rabbitTemplate) {
        super();
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarEnvio(EnvioRequestDto request) {
        EnvioFacturaEvent event = new EnvioFacturaEvent(request.getIdFactura(), request.getCorreo());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_ENVIAR, event);
        log.info("envio encolado: {}", event);
    }
}
