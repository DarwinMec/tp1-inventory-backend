package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.SaleDTO;

import java.util.List;
import java.util.UUID;

public interface SaleService {

    List<SaleDTO> getAll();

    SaleDTO getById(UUID id);

    SaleDTO create(SaleDTO dto);
}
