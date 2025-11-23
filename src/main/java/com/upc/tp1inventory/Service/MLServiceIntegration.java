package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.*;

import java.util.List;
import java.util.UUID;

public interface MLServiceIntegration {

    boolean isMLServiceHealthy();

    MLServiceHealthDTO getMLServiceHealth();

    MLTrainResponseDTO trainModel(MLTrainRequestDTO request);

    MLPredictionResponseDTO generatePredictions(MLPredictionRequestDTO request);

    List<MLServicePredictionDTO> getPredictionsForDish(UUID dishId, Integer weeksAhead);

    List<MLServicePredictionDTO> getLatestPredictions();

    MLModelInfoDTO getActiveModelInfo();

    MLPredictionResponseDTO syncPredictionsToDatabase(Integer weeksAhead);
}