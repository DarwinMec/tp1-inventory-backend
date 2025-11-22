package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.*;
import com.upc.tp1inventory.Entity.*;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.*;
import com.upc.tp1inventory.Service.DishService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final ProductRepository productRepository;
    private final DishIngredientRepository dishIngredientRepository;

    public DishServiceImpl(DishRepository dishRepository,
                           ProductRepository productRepository,
                           DishIngredientRepository dishIngredientRepository) {
        this.dishRepository = dishRepository;
        this.productRepository = productRepository;
        this.dishIngredientRepository = dishIngredientRepository;
    }

    @Override
    public List<DishDTO> getAllDishes() {
        return dishRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DishDTO getDishById(UUID id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found: " + id));
        return toDTO(dish);
    }

    @Override
    public DishDTO createDish(DishDTO dto) {
        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setCategory(dto.getCategory());
        dish.setPrice(dto.getPrice());
        dish.setIsActive(dto.getIsActive());
        dish.setPreparationTime(dto.getPreparationTime());

        Dish savedDish = dishRepository.save(dish);

        // Save ingredients
        if (dto.getIngredients() != null) {
            for (DishIngredientDTO ing : dto.getIngredients()) {
                DishIngredient di = new DishIngredient();
                di.setDish(savedDish);
                di.setProduct(productRepository.findById(ing.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
                di.setQuantityNeeded(ing.getQuantityNeeded());
                di.setUnit(ing.getUnit());
                di.setCostPerUnit(ing.getCostPerUnit());
                dishIngredientRepository.save(di);
            }
        }

        return toDTO(savedDish);
    }

    @Override
    public DishDTO updateDish(UUID id, DishDTO dto) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found"));

        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setCategory(dto.getCategory());
        dish.setPrice(dto.getPrice());
        dish.setIsActive(dto.getIsActive());
        dish.setPreparationTime(dto.getPreparationTime());

        Dish updated = dishRepository.save(dish);

        // Update ingredients (delete old, insert new)
        dishIngredientRepository.deleteAll(updated.getIngredients());

        for (DishIngredientDTO ing : dto.getIngredients()) {
            DishIngredient di = new DishIngredient();
            di.setDish(updated);
            di.setProduct(productRepository.findById(ing.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
            di.setQuantityNeeded(ing.getQuantityNeeded());
            di.setUnit(ing.getUnit());
            di.setCostPerUnit(ing.getCostPerUnit());
            dishIngredientRepository.save(di);
        }

        return toDTO(updated);
    }

    @Override
    public void deleteDish(UUID id) {
        dishRepository.deleteById(id);
    }

    private DishDTO toDTO(Dish dish) {
        DishDTO dto = new DishDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setCategory(dish.getCategory());
        dto.setPrice(dish.getPrice());
        dto.setIsActive(dish.getIsActive());
        dto.setPreparationTime(dish.getPreparationTime());

        if (dish.getIngredients() != null) {
            dto.setIngredients(
                    dish.getIngredients().stream().map(ing -> {
                        DishIngredientDTO i = new DishIngredientDTO();
                        i.setId(ing.getId());
                        i.setProductId(ing.getProduct().getId());
                        i.setProductName(ing.getProduct().getName());
                        i.setQuantityNeeded(ing.getQuantityNeeded());
                        i.setUnit(ing.getUnit());
                        i.setCostPerUnit(ing.getCostPerUnit());
                        return i;
                    }).collect(Collectors.toList())
            );
        }

        return dto;
    }
}
