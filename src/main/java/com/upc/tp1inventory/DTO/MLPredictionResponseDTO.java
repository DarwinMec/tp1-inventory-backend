package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MLPredictionResponseDTO {
    private Boolean success;
    private List<MLServicePredictionDTO> predictions;
    private Integer totalPredictions;
    private String modelId;
    private String message;
}
