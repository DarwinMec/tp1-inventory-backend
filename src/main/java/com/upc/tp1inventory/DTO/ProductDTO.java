package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductDTO {

    private UUID id;
    private String name;
    private String description;

    private String unitMeasure;
    private Integer minStock;
    private Integer maxStock;
    private Integer reorderPoint;
    private BigDecimal unitCost;
    private Boolean isActive;

    private UUID categoryId;     // FK
    private String categoryName; // para mostrar en listados
}
