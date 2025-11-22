package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SalesReportRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
