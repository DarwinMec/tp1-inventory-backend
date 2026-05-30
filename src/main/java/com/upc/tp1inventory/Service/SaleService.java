package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.SaleDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface SaleService {

    List<SaleDTO> getAll();

    SaleDTO getById(UUID id);

    SaleDTO create(SaleDTO dto, String username);

    Page<SaleDTO> getAllPaginated(int page, int size);
}