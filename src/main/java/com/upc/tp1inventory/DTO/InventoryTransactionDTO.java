package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InventoryTransactionDTO {

    private UUID id;

    private UUID productId;
    private String productName;

    private String transactionType; // inbound / outbound / adjustment

    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalCost;

    private UUID supplierId;
    private String supplierName;

    private String referenceNumber;
    private String notes;

    private LocalDateTime transactionDate;
}
