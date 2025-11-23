package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MLTrainResponseDTO {
    private String modelId;
    private String trainingHistoryId;
    private String modelPath;
    private Map<String, Object> metrics;
    private String version;
    private String createdBy;
    private Boolean success;
    private String message;
}
