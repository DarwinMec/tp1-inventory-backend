package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.MLModelDTO;

import java.util.List;
import java.util.UUID;

public interface MLModelService {

    List<MLModelDTO> getAll();

    MLModelDTO getById(UUID id);

    MLModelDTO create(MLModelDTO dto, String username);

    MLModelDTO update(UUID id, MLModelDTO dto);

    MLModelDTO setActive(UUID id, boolean active);

    MLModelDTO getActiveByName(String modelName);
}
