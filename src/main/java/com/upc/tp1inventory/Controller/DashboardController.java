package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.DashboardStatsDto;
import com.upc.tp1inventory.DTO.DashboardTendenciaDto;
import com.upc.tp1inventory.Service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/tendencias")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<List<DashboardTendenciaDto>> getTendencias(
            @RequestParam(name = "meses", defaultValue = "6") int meses
    ) {
        return ResponseEntity.ok(dashboardService.getTendencias(meses));
    }
}
