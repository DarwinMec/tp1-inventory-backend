package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.ReportDTO;
import com.upc.tp1inventory.DTO.SalesReportRequestDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ReportDTO> getAll() {
        return reportService.getAll();
    }

    @PostMapping("/sales-summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> generateSalesSummary(@RequestBody SalesReportRequestDTO request,
                                                          @AuthenticationPrincipal User currentUser) {
        ReportDTO dto = reportService.generateSalesSummary(request, currentUser.getUsername());
        return ResponseEntity.ok(dto);
    }
}
