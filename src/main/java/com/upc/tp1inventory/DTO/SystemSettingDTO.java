package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class SystemSettingDTO {

    private UUID id;
    private String settingKey;
    private String settingValue;
    private String dataType;
    private String description;
    private String updatedByUsername;
    private LocalDateTime updatedAt;
}
