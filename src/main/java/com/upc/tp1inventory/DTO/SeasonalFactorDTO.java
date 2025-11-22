package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SeasonalFactorDTO {

    private UUID id;
    private Integer month;
    private Integer dayOfWeek;
    private BigDecimal factor;
    private String description;
    private LocalDateTime createdAt;
}
