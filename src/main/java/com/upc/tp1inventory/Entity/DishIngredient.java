package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dish_ingredients")
@Getter
@Setter
public class DishIngredient {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Double quantityNeeded;

    private String unit;

    private Double costPerUnit;

    private LocalDateTime createdAt = LocalDateTime.now();
}
