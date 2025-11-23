package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MLPredictionRequestDTO {
    private UUID dishId;        // null = todos los platos
    private Integer weeksAhead = 4;
    private Boolean saveToDb = true;
}
