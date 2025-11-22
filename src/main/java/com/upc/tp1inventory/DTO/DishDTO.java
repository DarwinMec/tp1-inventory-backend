package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DishDTO {
    private UUID id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private Boolean isActive;
    private Integer preparationTime;

    private List<DishIngredientDTO> ingredients;
}
