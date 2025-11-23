package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WeeklyGlobalSupplyItemDTO {
    private UUID productId;
    private String productName;
    private String unitMeasure;

    // Total requerido por la semana (sumado de todos los platos)
    private Double totalRequired;

    // Stock actual y disponible desde Inventory
    private Double currentStock;
    private Double availableStock;

    // Lo que se debería comprar (max(totalRequired - availableStock, 0))
    private Double quantityToBuy;
}
