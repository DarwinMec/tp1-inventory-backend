package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.PredictionDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PredictionService {

    List<PredictionDTO> getAll();

    List<PredictionDTO> getByDish(UUID dishId);

    List<PredictionDTO> getByDishAndDateRange(UUID dishId, LocalDate start, LocalDate end);

    List<PredictionDTO> getByDateRange(LocalDate start, LocalDate end);

    // pensado para el script Python: enviar varias predicciones de una sola vez
    List<PredictionDTO> bulkCreate(List<PredictionDTO> dtos, String username);
}
