package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.demo.bean.dto.FacturaPdfDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String remitente;

    public CorreoService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        super();
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void enviarFactura(String destino, FacturaPdfDto factura) {
        try {
            Context context = new Context(Locale.forLanguageTag("es-PE"));
            context.setVariable("factura", factura);
            context.setVariable("correo", destino);
            String contenido = templateEngine.process("correo/pedido-realizado", context);

            byte[] pdf = descargar(factura.getUrlPdf());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destino);
            helper.setSubject("Pedido realizado - Factura " + factura.getIdFactura());
            helper.setText(contenido, true);
            helper.addAttachment("factura-" + factura.getIdFactura() + ".pdf", new ByteArrayResource(pdf),
                    "application/pdf");
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("No se pudo enviar la factura " + factura.getIdFactura(), e);
        }
    }

    private byte[] descargar(String urlPdf) throws IOException {
        try (InputStream in = URI.create(urlPdf).toURL().openStream()) {
            return in.readAllBytes();
        }
    }
}
