package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.DashboardStatsDto;
import com.upc.tp1inventory.DTO.DashboardTendenciaDto;

import java.util.List;

public interface DashboardService {

    DashboardStatsDto getStats();

    /**
     * Tendencias de ventas por mes (últimos N meses)
     */
    List<DashboardTendenciaDto> getTendencias(int meses);
}
