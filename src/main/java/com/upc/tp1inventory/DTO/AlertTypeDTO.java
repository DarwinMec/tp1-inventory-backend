package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AlertTypeDTO {

    private UUID id;
    private String name;
    private String description;
    private String severity;
    private Boolean isActive;
}
