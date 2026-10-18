package com.example.demo.bean.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class FacturaPdfDto {

    private Long idFactura;
    private Long idCliente;
    private String clienteDenominacion;
    private String clienteNumeroDocumento;
    private String serie;
    private Integer numero;
    private String fechaEmision;
    private String estado;
    private String moneda;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private List<DetalleFacturaDto> detalles;
    private String urlPdf;
}
