package com.example.demo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.bean.dto.FacturaPdfDto;

@FeignClient(name = "ms-factura")
public interface FacturaClient {

    @GetMapping("/api/facturas/{id}")
    FacturaPdfDto obtener(@PathVariable("id") Long id);
}