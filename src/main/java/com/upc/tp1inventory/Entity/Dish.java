package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
@Getter
@Setter
public class Dish {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String description;

    private String category;

    private Double price;

    private Boolean isActive = true;

    private Integer preparationTime;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DishIngredient> ingredients;
}
