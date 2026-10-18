package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.bean.dto.EnvioRequestDto;
import com.example.demo.rabbitmq.EnvioProductor;

@RestController
@RequestMapping("/envios")
public class EnvioController {

    private final EnvioProductor envioProductor;

    public EnvioController(EnvioProductor envioProductor) {
        super();
        this.envioProductor = envioProductor;
    }

    @PostMapping
    public ResponseEntity<Void> enviar(@RequestBody EnvioRequestDto request) {
        envioProductor.publicarEnvio(request);
        return ResponseEntity.accepted().build();
    }
}
