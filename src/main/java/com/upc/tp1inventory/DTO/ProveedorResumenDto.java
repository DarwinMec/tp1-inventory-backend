package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProveedorResumenDto {

    private UUID id;
    private String nombre;

    // Opcionales, el frontend los trata como opcionales
    private Double calificacion;        // por ahora null
    private List<String> insumosClave;  // por ahora null o lista vacía
}
