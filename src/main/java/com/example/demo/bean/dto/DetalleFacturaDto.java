package com.example.demo.bean.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DetalleFacturaDto {

    private String codigoProducto;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal total;
}
