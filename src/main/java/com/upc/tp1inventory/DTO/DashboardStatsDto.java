package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardStatsDto {

    // 👇 nombres EXACTOS a los que el frontend espera
    private long totalInsumos;
    private long totalPlatillos;
    private long totalProveedores;

    private BigDecimal ventasHoy;

    // % variación vs ayer
    private double variacionVentasPorcentaje;

    // rotación de inventario (métrica simple, la calculamos en el servicio)
    private double rotacionInventario;

    // nivel de servicio (0–100)
    private double nivelServicio;
}
