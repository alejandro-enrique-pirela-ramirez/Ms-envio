package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.bean.dto.EnvioRequestDto;
import com.example.demo.rabbitmq.EnvioProductor;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioProductor envioProductor;

    @PostMapping
    public ResponseEntity<Void> enviar(@RequestBody EnvioRequestDto request) {
        envioProductor.publicarEnvio(request);
        return ResponseEntity.accepted().build();
    }
}