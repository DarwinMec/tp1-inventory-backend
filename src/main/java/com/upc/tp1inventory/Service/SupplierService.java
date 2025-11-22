package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.SupplierDTO;

import java.util.List;
import java.util.UUID;

public interface SupplierService {

    List<SupplierDTO> getAll();

    SupplierDTO getById(UUID id);

    SupplierDTO create(SupplierDTO dto);

    SupplierDTO update(UUID id, SupplierDTO dto);

    void delete(UUID id);
}
