package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.ProductDTO;
import com.upc.tp1inventory.Entity.Category;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.CategoryRepository;
import com.upc.tp1inventory.Repository.ProductRepository;
import com.upc.tp1inventory.Service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductDTO> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        return toDTO(product);
    }

    @Override
    public ProductDTO create(ProductDTO dto) {
        Product product = new Product();
        applyFromDTO(dto, product);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    @Override
    public ProductDTO update(UUID id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        applyFromDTO(dto, product);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Producto no encontrado: " + id);
        }
        productRepository.deleteById(id);
    }

    private void applyFromDTO(ProductDTO dto, Product product) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setUnitMeasure(dto.getUnitMeasure());
        product.setMinStock(dto.getMinStock());
        product.setMaxStock(dto.getMaxStock());
        product.setReorderPoint(dto.getReorderPoint());
        product.setUnitCost(dto.getUnitCost());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada: " + dto.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setUnitMeasure(product.getUnitMeasure());
        dto.setMinStock(product.getMinStock());
        dto.setMaxStock(product.getMaxStock());
        dto.setReorderPoint(product.getReorderPoint());
        dto.setUnitCost(product.getUnitCost());
        dto.setIsActive(product.getIsActive());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }
}
