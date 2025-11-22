package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.ModelTrainingHistoryDTO;

import java.util.List;
import java.util.UUID;

public interface ModelTrainingHistoryService {

    List<ModelTrainingHistoryDTO> getByModel(UUID modelId);

    ModelTrainingHistoryDTO create(ModelTrainingHistoryDTO dto, String username);
}
