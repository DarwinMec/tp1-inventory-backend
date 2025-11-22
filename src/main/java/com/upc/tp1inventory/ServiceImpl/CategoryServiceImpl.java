package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.CategoryDTO;
import com.upc.tp1inventory.Entity.Category;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.CategoryRepository;
import com.upc.tp1inventory.Service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));
        return toDTO(category);
    }

    @Override
    public CategoryDTO create(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Override
    public CategoryDTO update(UUID id, CategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + id));

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria no encontrada: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
