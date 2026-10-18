package com.example.demo.rabbitmq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.example.demo.bean.dto.FacturaPdfDto;
import com.example.demo.bean.event.EnvioFacturaEvent;
import com.example.demo.client.FacturaClient;
import com.example.demo.service.CorreoService;

class EnvioConsumidorTest {

    private final FacturaClient facturaClient = mock(FacturaClient.class);
    private final CorreoService correoService = mock(CorreoService.class);
    private final EnvioConsumidor consumidor = new EnvioConsumidor(facturaClient, correoService);

    @Test
    void entregaLaFacturaCompletaAlServicioDeCorreo() {
        FacturaPdfDto factura = new FacturaPdfDto();
        factura.setIdFactura(42L);
        factura.setUrlPdf("https://ejemplo.com/factura.pdf");
        when(facturaClient.obtener(42L)).thenReturn(factura);

        consumidor.enviar(new EnvioFacturaEvent(42L, "cliente@ejemplo.com"));

        verify(correoService).enviarFactura("cliente@ejemplo.com", factura);
    }

    @Test
    void conservaElDescarteDeFacturasSinPdf() {
        FacturaPdfDto factura = new FacturaPdfDto();
        when(facturaClient.obtener(42L)).thenReturn(factura);

        consumidor.enviar(new EnvioFacturaEvent(42L, "cliente@ejemplo.com"));
        factura.setUrlPdf("   ");
        consumidor.enviar(new EnvioFacturaEvent(42L, "cliente@ejemplo.com"));

        verifyNoInteractions(correoService);
    }
}
