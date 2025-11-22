package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ReportScheduleDTO {

    private UUID id;

    private String reportType;
    private String title;
    private String scheduleType;  // daily, weekly, monthly

    private Map<String, Object> parameters;

    private Boolean isActive;
    private LocalDateTime lastGenerated;
    private LocalDateTime nextGeneration;

    private String createdByUsername;
    private LocalDateTime createdAt;
}
