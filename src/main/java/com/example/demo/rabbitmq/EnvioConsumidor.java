package com.example.demo.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.demo.bean.dto.FacturaPdfDto;
import com.example.demo.bean.event.EnvioFacturaEvent;
import com.example.demo.client.FacturaClient;
import com.example.demo.config.RabbitMqConfig;
import com.example.demo.service.CorreoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnvioConsumidor {

    private final FacturaClient facturaClient;
    private final CorreoService correoService;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_ENVIAR)
    public void enviar(EnvioFacturaEvent event) {
        FacturaPdfDto factura = facturaClient.obtener(event.idFactura());

        if (factura.getUrlPdf() == null || factura.getUrlPdf().isBlank()) {
            log.warn("factura {} sin urlPdf, envio descartado", event.idFactura());
            return;
        }

        correoService.enviarFactura(event.correo(), event.idFactura(), factura.getUrlPdf());
        log.info("factura {} enviada a {}", event.idFactura(), event.correo());
    }
}