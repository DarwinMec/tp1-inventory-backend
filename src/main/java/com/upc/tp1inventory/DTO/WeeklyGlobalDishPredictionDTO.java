package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WeeklyGlobalDishPredictionDTO {
    private UUID dishId;
    private String dishName;
    private String weekStart;       // ISO string, ej: "2025-01-27"
    private Double predictedDemand;
    private String confidence;      // "low", "medium", "high"
}
