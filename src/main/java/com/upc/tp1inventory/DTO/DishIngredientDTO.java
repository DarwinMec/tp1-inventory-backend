package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DishIngredientDTO {

    private UUID id;

    private UUID productId;
    private String productName;

    private BigDecimal quantityNeeded;
    private String unit;
    private BigDecimal costPerUnit;
}