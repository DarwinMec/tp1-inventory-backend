package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class DishIngredientDTO {
    private UUID id;
    private UUID productId;
    private String productName;
    private Double quantityNeeded;
    private String unit;
    private Double costPerUnit;
}
