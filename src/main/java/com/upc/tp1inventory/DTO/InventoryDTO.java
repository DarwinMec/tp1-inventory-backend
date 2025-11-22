package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InventoryDTO {

    private UUID productId;
    private String productName;
    private BigDecimal currentStock;
    private BigDecimal availableStock;
    private BigDecimal reservedStock;
    private LocalDateTime lastUpdated;
}
