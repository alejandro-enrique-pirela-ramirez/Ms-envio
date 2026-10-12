package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CorreoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    public void enviarFactura(String destino, Long idFactura, String urlPdf) {
        try {
            byte[] pdf = descargar(urlPdf);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destino);
            helper.setSubject("Factura electrónica " + idFactura);
            helper.setText("Adjuntamos su factura electrónica en formato PDF.");
            helper.addAttachment("factura-" + idFactura + ".pdf", new ByteArrayResource(pdf));
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("No se pudo enviar la factura " + idFactura, e);
        }
    }

    private byte[] descargar(String urlPdf) throws IOException {
        try (InputStream in = URI.create(urlPdf).toURL().openStream()) {
            return in.readAllBytes();
        }
    }
}