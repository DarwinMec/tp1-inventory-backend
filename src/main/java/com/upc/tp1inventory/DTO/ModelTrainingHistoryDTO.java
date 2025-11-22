package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ModelTrainingHistoryDTO {

    private UUID id;
    private UUID modelId;
    private String modelName;
    private LocalDateTime trainingStart;
    private LocalDateTime trainingEnd;
    private Integer dataPointsUsed;
    private BigDecimal accuracyBefore;
    private BigDecimal accuracyAfter;
    private String status;
    private String errorMessage;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
