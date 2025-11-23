package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO para representar predicciones que vienen del servicio Python
 */
@Getter
@Setter
public class MLServicePredictionDTO {
    private String dishId;      // Como String porque viene de Python
    private String dishName;
    private String weekStart;   // Formato: "2025-01-27"
    private Double predictedDemand;
    private String confidence;  // "low", "medium", "high"
}
