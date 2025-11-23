package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.WeeklyGlobalPredictionResponseDTO;

public interface WeeklyGlobalSupplyService {

    /**
     * Genera el plan global de abastecimiento para la(s) próxima(s) semana(s).
     *
     * @param weeksAhead cuántas semanas hacia adelante (normalmente 1)
     * @return DTO con predicciones por plato e insumos agregados
     */
    WeeklyGlobalPredictionResponseDTO getWeeklyGlobalPlan(Integer weeksAhead);
}
