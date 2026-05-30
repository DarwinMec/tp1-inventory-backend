package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WeeklyGlobalSupplyItemDTO {

    private UUID productId;
    private String productName;
    private String unitMeasure;

    // Total requerido por la semana (sumado de todos los platos)
    private BigDecimal totalRequired;

    // Stock actual y disponible desde Inventory
    private BigDecimal currentStock;
    private BigDecimal availableStock;

    // Lo que se debería comprar (max(totalRequired - availableStock, 0))
    private BigDecimal quantityToBuy;
}