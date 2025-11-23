package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MLServiceHealthDTO {
    private String status;      // "ok", "error", "unavailable"
    private String service;
    private String version;
    private String message;
}