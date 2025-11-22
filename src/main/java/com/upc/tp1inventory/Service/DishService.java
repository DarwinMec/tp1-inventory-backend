package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.DishDTO;
import java.util.List;
import java.util.UUID;

public interface DishService {

    List<DishDTO> getAllDishes();

    DishDTO getDishById(UUID id);

    DishDTO createDish(DishDTO dishDTO);

    DishDTO updateDish(UUID id, DishDTO dishDTO);

    void deleteDish(UUID id);
}
