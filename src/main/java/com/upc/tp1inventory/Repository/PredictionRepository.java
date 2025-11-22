package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, UUID> {

    // Todas las predicciones de un plato
    List<Prediction> findByDish_IdOrderByPredictedDateAsc(UUID dishId);

    // Predicciones por plato y rango de fechas
    List<Prediction> findByDish_IdAndPredictedDateBetweenOrderByPredictedDateAsc(
            UUID dishId,
            LocalDate start,
            LocalDate end
    );

    // Predicciones por rango de fechas (para dashboard general)
    List<Prediction> findByPredictedDateBetweenOrderByPredictedDateAsc(
            LocalDate start,
            LocalDate end
    );
}
