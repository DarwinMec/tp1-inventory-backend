package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.ReportDTO;
import com.upc.tp1inventory.DTO.SalesReportRequestDTO;

import java.util.List;

public interface ReportService {

    List<ReportDTO> getAll();

    ReportDTO generateSalesSummary(SalesReportRequestDTO request, String username);
}
