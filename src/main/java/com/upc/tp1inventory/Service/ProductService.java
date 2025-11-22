package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.ProductDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<ProductDTO> getAll();

    ProductDTO getById(UUID id);

    ProductDTO create(ProductDTO dto);

    ProductDTO update(UUID id, ProductDTO dto);

    void delete(UUID id);
}
