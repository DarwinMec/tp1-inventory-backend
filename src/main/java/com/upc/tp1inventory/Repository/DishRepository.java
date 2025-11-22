package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {
}
