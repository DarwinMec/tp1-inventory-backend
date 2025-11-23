package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeeklyGlobalPredictionResponseDTO {

    // Semana objetivo (ej: "2025-01-27")
    private String weekStart;

    // Predicciones por plato
    private List<WeeklyGlobalDishPredictionDTO> dishes;

    // Lista consolidada de insumos para toda la semana
    private List<WeeklyGlobalSupplyItemDTO> supplies;
}
