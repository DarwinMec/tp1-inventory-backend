package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.DishDTO;
import com.upc.tp1inventory.DTO.DishIngredientDTO;
import com.upc.tp1inventory.Entity.Dish;
import com.upc.tp1inventory.Entity.DishIngredient;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.DishIngredientRepository;
import com.upc.tp1inventory.Repository.DishRepository;
import com.upc.tp1inventory.Repository.ProductRepository;
import com.upc.tp1inventory.Service.DishService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        return dishRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DishDTO getDishById(UUID id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + id));

        return toDTO(dish);
    }

    @Override
    public DishDTO createDish(DishDTO dto) {

        validateDish(dto);

        Dish dish = new Dish();
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setCategory(dto.getCategory());
        dish.setPrice(dto.getPrice());
        dish.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        dish.setPreparationTime(dto.getPreparationTime());

        Dish savedDish = dishRepository.save(dish);

        if (dto.getIngredients() != null) {
            saveIngredients(savedDish, dto.getIngredients());
        }

        Dish reloadedDish = dishRepository.findById(savedDish.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado luego de crearlo"));

        return toDTO(reloadedDish);
    }

    @Override
    public DishDTO updateDish(UUID id, DishDTO dto) {

        validateDish(dto);

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + id));

        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setCategory(dto.getCategory());
        dish.setPrice(dto.getPrice());
        dish.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : dish.getIsActive());
        dish.setPreparationTime(dto.getPreparationTime());

        Dish updatedDish = dishRepository.save(dish);

        /*
         * Si ingredients viene null, se actualizan solo los datos generales del plato.
         * Si viene lista vacía [], se elimina la receta.
         * Si viene con datos, se reemplaza la receta completa.
         */
        if (dto.getIngredients() != null) {
            List<DishIngredient> currentIngredients = dishIngredientRepository.findByDish(updatedDish);
            dishIngredientRepository.deleteAll(currentIngredients);

            saveIngredients(updatedDish, dto.getIngredients());
        }

        Dish reloadedDish = dishRepository.findById(updatedDish.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado luego de actualizarlo"));

        return toDTO(reloadedDish);
    }

    @Override
    public void deleteDish(UUID id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + id));

        dishRepository.delete(dish);
    }

    private void saveIngredients(Dish dish, List<DishIngredientDTO> ingredients) {

        for (DishIngredientDTO ing : ingredients) {

            if (ing.getProductId() == null) {
                throw new IllegalArgumentException("Cada ingrediente debe tener un productId");
            }

            if (ing.getQuantityNeeded() == null || ing.getQuantityNeeded().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad requerida del ingrediente debe ser mayor a cero");
            }

            Product product = productRepository.findById(ing.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + ing.getProductId()));

            DishIngredient dishIngredient = new DishIngredient();
            dishIngredient.setDish(dish);
            dishIngredient.setProduct(product);
            dishIngredient.setQuantityNeeded(ing.getQuantityNeeded());
            dishIngredient.setUnit(ing.getUnit());
            dishIngredient.setCostPerUnit(ing.getCostPerUnit());

            dishIngredientRepository.save(dishIngredient);
        }
    }

    private void validateDish(DishDTO dto) {

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del plato es obligatorio");
        }

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio del plato debe ser mayor a cero");
        }
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

        List<DishIngredient> ingredients = dishIngredientRepository.findByDish(dish);

        dto.setIngredients(
                ingredients.stream().map(ing -> {
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

        return dto;
    }
}