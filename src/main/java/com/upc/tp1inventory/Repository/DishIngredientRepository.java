package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.Dish;
import com.upc.tp1inventory.Entity.DishIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DishIngredientRepository extends JpaRepository<DishIngredient, UUID> {
    List<DishIngredient> findByDish(Dish dish);

}
