package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.*;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.MLServiceIntegration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.upc.tp1inventory.Service.WeeklyGlobalSupplyService;


import java.util.List;
import java.util.UUID;

/**
 * Controlador para integración con el servicio de Machine Learning en Python (FastAPI)
 *
 * Este controlador se diferencia de PredictionController en que:
 * - PredictionController: gestiona predicciones almacenadas en la BD local
 * - MLServiceController: se comunica con el servicio externo de Python para ML
 *
 * Endpoints base: /api/ml-service
 */
@RestController
@RequestMapping("/api/ml-service")
@CrossOrigin(origins = "*")
public class MLServiceController {

    private final MLServiceIntegration mlServiceIntegration;
    private final WeeklyGlobalSupplyService weeklyGlobalSupplyService;

    public MLServiceController(MLServiceIntegration mlServiceIntegration,
                               WeeklyGlobalSupplyService weeklyGlobalSupplyService) {
        this.mlServiceIntegration = mlServiceIntegration;
        this.weeklyGlobalSupplyService = weeklyGlobalSupplyService;
    }

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<MLServiceHealthDTO> checkHealth() {
        try {
            MLServiceHealthDTO health = mlServiceIntegration.getMLServiceHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            MLServiceHealthDTO error = new MLServiceHealthDTO();
            error.setStatus("error");
            error.setMessage("Error verificando servicio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/train")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLTrainResponseDTO> trainModel(
            @RequestBody MLTrainRequestDTO request,
            @AuthenticationPrincipal User currentUser) {

        try {
            // Si no viene createdBy, usar el usuario autenticado
            if (request.getCreatedBy() == null && currentUser != null) {
                request.setCreatedBy(currentUser.getUsername());
            }

            MLTrainResponseDTO response = mlServiceIntegration.trainModel(request);

            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            MLTrainResponseDTO error = new MLTrainResponseDTO();
            error.setSuccess(false);
            error.setMessage("Error en entrenamiento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/predict")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<MLPredictionResponseDTO> generatePredictions(
            @RequestBody MLPredictionRequestDTO request) {

        try {
            MLPredictionResponseDTO response = mlServiceIntegration.generatePredictions(request);

            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            MLPredictionResponseDTO error = new MLPredictionResponseDTO();
            error.setSuccess(false);
            error.setMessage("Error generando predicciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/predict/dish/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<List<MLServicePredictionDTO>> getPredictionsForDish(
            @PathVariable UUID dishId,
            @RequestParam(defaultValue = "4") Integer weeks) {

        try {
            List<MLServicePredictionDTO> predictions =
                    mlServiceIntegration.getPredictionsForDish(dishId, weeks);

            return ResponseEntity.ok(predictions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @GetMapping("/predictions/latest")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<List<MLServicePredictionDTO>> getLatestPredictions() {
        try {
            List<MLServicePredictionDTO> predictions =
                    mlServiceIntegration.getLatestPredictions();

            return ResponseEntity.ok(predictions);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of());
        }
    }

    @GetMapping("/model/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLModelInfoDTO> getActiveModelInfo() {
        try {
            MLModelInfoDTO info = mlServiceIntegration.getActiveModelInfo();
            return ResponseEntity.ok(info);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MLModelInfoDTO());
        }
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MLPredictionResponseDTO> syncPredictions(
            @RequestParam(defaultValue = "4") Integer weeks) {

        try {
            MLPredictionResponseDTO response =
                    mlServiceIntegration.syncPredictionsToDatabase(weeks);

            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            MLPredictionResponseDTO error = new MLPredictionResponseDTO();
            error.setSuccess(false);
            error.setMessage("Error sincronizando predicciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/quick-forecast/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<MLServicePredictionDTO> getQuickForecast(
            @PathVariable UUID dishId) {

        try {
            List<MLServicePredictionDTO> predictions =
                    mlServiceIntegration.getPredictionsForDish(dishId, 1);

            if (predictions.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(predictions.get(0));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MLServicePredictionDTO());
        }
    }

    /**
     * Predicción semanal global:
     * - Predicciones de demanda por plato
     * - Lista consolidada de insumos y cantidades a comprar
     */
    @GetMapping("/weekly-supply")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<WeeklyGlobalPredictionResponseDTO> getWeeklySupplyPlan(
            @RequestParam(name = "weeksAhead", required = false, defaultValue = "1") Integer weeksAhead
    ) {
        WeeklyGlobalPredictionResponseDTO response =
                weeklyGlobalSupplyService.getWeeklyGlobalPlan(weeksAhead);

        return ResponseEntity.ok(response);
    }


}