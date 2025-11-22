package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class SaleItemDTO {

    private UUID id;

    private UUID dishId;
    private String dishName;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
