package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.DishDTO;
import com.upc.tp1inventory.Service.DishService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dishes")
@CrossOrigin(origins = "*")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<DishDTO> getAll() {
        return dishService.getAllDishes();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public DishDTO getById(@PathVariable UUID id) {
        return dishService.getDishById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public DishDTO create(@RequestBody DishDTO dto) {
        return dishService.createDish(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public DishDTO update(@PathVariable UUID id, @RequestBody DishDTO dto) {
        return dishService.updateDish(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public void delete(@PathVariable UUID id) {
        dishService.deleteDish(id);
    }
}
