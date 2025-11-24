package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class MLModelDTO {

    private UUID id;
    private String modelName;
    private String modelType;
    private String version;
    private Map<String, Object> parameters;
    private Boolean isActive;
    private BigDecimal r2;
    private BigDecimal mae;
    private BigDecimal rmse;
    private LocalDateTime trainedAt;
    private String createdByUsername;
    private LocalDateTime createdAt;
}
