package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ReportDTO {

    private UUID id;
    private String reportType;
    private String title;
    private Map<String, Object> parametersJson;  // ✅ ahora Map, no String
    private String filePath;
    private String fileFormat;
    private String generatedByUsername;
    private LocalDateTime generatedAt;
}
