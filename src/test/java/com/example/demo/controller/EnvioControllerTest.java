package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.bean.dto.EnvioRequestDto;
import com.example.demo.rabbitmq.EnvioProductor;

class EnvioControllerTest {

    @Test
    void enviarFacturaConservaLaRutaYEncolaLaSolicitud() throws Exception {
        EnvioProductor productor = mock(EnvioProductor.class);
        var mvc = MockMvcBuilders.standaloneSetup(new EnvioController(productor)).build();

        mvc.perform(post("/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"idFactura":42,"correo":"cliente@ejemplo.com"}
                        """))
                .andExpect(status().isAccepted());

        ArgumentCaptor<EnvioRequestDto> request = ArgumentCaptor.forClass(EnvioRequestDto.class);
        verify(productor).publicarEnvio(request.capture());
        assertThat(request.getValue().getIdFactura()).isEqualTo(42L);
        assertThat(request.getValue().getCorreo()).isEqualTo("cliente@ejemplo.com");
    }
}
