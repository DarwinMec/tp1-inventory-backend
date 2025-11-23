package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MLTrainRequestDTO {
    private String createdBy;
    private Boolean asyncMode = false;
}