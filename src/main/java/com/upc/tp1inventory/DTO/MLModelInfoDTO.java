package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para información del modelo activo desde Python
 */
@Getter
@Setter
public class MLModelInfoDTO {
    private String modelId;
    private String modelName;
    private String modelType;
    private String version;
    private BigDecimal mae;
    private BigDecimal rmse;
    private BigDecimal r2;
    private String trainedAt;
    private String createdAt;
}