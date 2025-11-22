package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.CategoryDTO;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<CategoryDTO> getAll();

    CategoryDTO getById(UUID id);

    CategoryDTO create(CategoryDTO dto);

    CategoryDTO update(UUID id, CategoryDTO dto);

    void delete(UUID id);
}
