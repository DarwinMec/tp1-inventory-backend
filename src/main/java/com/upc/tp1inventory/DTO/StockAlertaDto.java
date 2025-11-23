package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StockAlertaDto {

    private UUID id;          // id del producto
    private String nombre;    // nombre del producto
    private String categoria; // nombre de la categoría
    private int stockActual;  // stock actual (entero)
    private int stockMinimo;  // stock mínimo configurado
}
