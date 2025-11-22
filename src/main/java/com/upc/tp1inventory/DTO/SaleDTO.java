package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SaleDTO {

    private UUID id;

    private LocalDate saleDate;
    private LocalTime saleTime;

    private BigDecimal totalAmount;

    private Integer dayOfWeek;
    private Integer month;
    private Integer year;

    private String weather;
    private Boolean isHoliday;
    private Boolean isWeekend;

    private List<SaleItemDTO> items;
}
