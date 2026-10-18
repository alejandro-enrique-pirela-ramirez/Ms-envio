package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.example.demo.bean.dto.DetalleFacturaDto;
import com.example.demo.bean.dto.FacturaPdfDto;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

class CorreoServiceTest {

    @TempDir
    Path directorioTemporal;

    private JavaMailSender mailSender;
    private CorreoService correoService;
    private MimeMessage message;
    private FacturaPdfDto factura;
    private byte[] pdf;

    @BeforeEach
    void preparar() throws IOException {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        mailSender = mock(JavaMailSender.class);
        message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        correoService = new CorreoService(mailSender, templateEngine);
        ReflectionTestUtils.setField(correoService, "remitente", "ventas@ejemplo.com");

        pdf = "%PDF-1.4\nfactura de prueba".getBytes(StandardCharsets.UTF_8);
        Path archivo = Files.write(directorioTemporal.resolve("factura.pdf"), pdf);
        factura = new FacturaPdfDto();
        factura.setIdFactura(42L);
        factura.setUrlPdf(archivo.toUri().toString());
    }

    @Test
    void enviaPedidoConDatosCompletosHtmlSeguroYPdfAdjunto() throws Exception {
        factura.setClienteDenominacion("Gases <Industriales> & Asociados");
        factura.setClienteNumeroDocumento("20123456789");
        factura.setSerie("F001");
        factura.setNumero(123);
        factura.setFechaEmision("2026-10-18");
        factura.setEstado("ACEPTADA");
        factura.setMoneda("PEN");
        factura.setSubtotal(new BigDecimal("300.00"));
        factura.setIgv(new BigDecimal("54.00"));
        factura.setTotal(new BigDecimal("354.00"));
        factura.setDetalles(List.of(
                detalle("OX-01", "Oxígeno <script>alert('x')</script>", "2.000", "100.00", "236.00"),
                detalle("NI-02", "Nitrógeno", "4.000", "25.00", "118.00")));

        correoService.enviarFactura("cliente@ejemplo.com", factura);
        message.saveChanges();

        assertThat(message.getSubject()).isEqualTo("Pedido realizado - Factura 42");
        assertThat(message.getFrom()[0].toString()).isEqualTo("ventas@ejemplo.com");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("cliente@ejemplo.com");
        Part cuerpo = buscarParte(message, "text/html");
        assertThat(cuerpo).isNotNull();
        assertThat(cuerpo.getContentType()).containsIgnoringCase("charset=UTF-8");
        String html = (String) cuerpo.getContent();
        assertThat(html).contains(
                "Pedido realizado", "Datos del cliente", "Productos y cantidades pedidas", "Monto total",
                "Datos de la factura", "Gases &lt;Industriales&gt; &amp; Asociados", "20123456789",
                "cliente@ejemplo.com", "OX-01", "NI-02", "Nitrógeno", "&lt;script&gt;", ">2</td>", ">4</td>",
                "300.00", "54.00", "354.00", "236.00", "118.00", "PEN", "F001-123", "2026-10-18", "ACEPTADA",
                "Muchas gracias por su pedido, estaremos contactándonos con usted para el cobro y entrega de los productos.");
        assertThat(html).doesNotContain("<script>", "th:text", "th:each");

        Part adjunto = buscarParte(message, "application/pdf");
        assertThat(adjunto).isNotNull();
        assertThat(adjunto.getFileName()).isEqualTo("factura-42.pdf");
        assertThat(adjunto.getInputStream().readAllBytes()).isEqualTo(pdf);
        verify(mailSender).send(message);
    }

    @Test
    void muestraDatosNoDisponiblesSinInventarImportes() throws Exception {
        correoService.enviarFactura("cliente@ejemplo.com", factura);
        message.saveChanges();

        String html = (String) buscarParte(message, "text/html").getContent();
        assertThat(html).contains("No disponible", "No hay detalle de productos disponible.");
        assertThat(html).doesNotContain("null", "0.00", "F001-1");
        verify(mailSender).send(message);
    }

    @Test
    void noEnviaCorreoCuandoNoPuedeDescargarElPdf() {
        factura.setUrlPdf(directorioTemporal.resolve("ausente.pdf").toUri().toString());

        assertThatThrownBy(() -> correoService.enviarFactura("cliente@ejemplo.com", factura))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No se pudo enviar la factura 42")
                .hasCauseInstanceOf(IOException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    private DetalleFacturaDto detalle(String codigo, String descripcion, String cantidad,
            String valorUnitario, String total) {
        DetalleFacturaDto detalle = new DetalleFacturaDto();
        detalle.setCodigoProducto(codigo);
        detalle.setDescripcion(descripcion);
        detalle.setUnidadMedida("NIU");
        detalle.setCantidad(new BigDecimal(cantidad));
        detalle.setValorUnitario(new BigDecimal(valorUnitario));
        detalle.setTotal(new BigDecimal(total));
        return detalle;
    }

    private Part buscarParte(Part parte, String tipo) throws Exception {
        if (parte.isMimeType(tipo)) {
            return parte;
        }
        if (parte.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) parte.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                Part encontrada = buscarParte(multipart.getBodyPart(i), tipo);
                if (encontrada != null) {
                    return encontrada;
                }
            }
        }
        return null;
    }
}
