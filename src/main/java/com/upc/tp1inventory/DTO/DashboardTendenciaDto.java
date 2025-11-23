package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardTendenciaDto {

    // Ej: "Ene", "Feb", "Mar"
    private String mes;

    private BigDecimal ventas;

    // Por ahora usamos una predicción sencilla basada en las ventas
    private BigDecimal prediccion;
}
