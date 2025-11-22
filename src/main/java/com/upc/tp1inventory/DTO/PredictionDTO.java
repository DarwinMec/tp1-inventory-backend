package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PredictionDTO {

    private UUID id;

    private UUID modelId;
    private String modelName;

    private UUID dishId;
    private String dishName;

    private LocalDate predictedDate;
    private Integer predictedQuantity;

    private BigDecimal confidenceLevel;
    private String weatherFactor;
    private BigDecimal seasonalFactor;
    private BigDecimal trendFactor;

    private String createdByUsername;
    private LocalDateTime createdAt;
}
