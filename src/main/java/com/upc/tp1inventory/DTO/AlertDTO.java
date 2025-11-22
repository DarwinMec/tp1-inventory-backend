package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AlertDTO {

    private UUID id;

    private UUID alertTypeId;
    private String alertTypeName;

    private String title;
    private String message;
    private String severity;

    private UUID productId;
    private String productName;

    private UUID dishId;
    private String dishName;

    private Boolean isRead;
    private Boolean isResolved;

    private String resolvedByUsername;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
}
