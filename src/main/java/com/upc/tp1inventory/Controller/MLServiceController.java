package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.*;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.MLServiceIntegration;
import com.upc.tp1inventory.Service.WeeklyGlobalSupplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        MLServiceHealthDTO health = mlServiceIntegration.getMLServiceHealth();
        return ResponseEntity.ok(health);
    }

    @PostMapping("/train")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLTrainResponseDTO> trainModel(
            @RequestBody(required = false) MLTrainRequestDTO request,
            @AuthenticationPrincipal User currentUser) {

        try {
            if (request == null) {
                request = new MLTrainRequestDTO();
            }

            if (request.getCreatedBy() == null && currentUser != null) {
                request.setCreatedBy(currentUser.getUsername());
            }

            if (request.getFastMode() == null) {
                request.setFastMode(true);
            }

            if (request.getRegisterInDb() == null) {
                request.setRegisterInDb(true);
            }

            MLTrainResponseDTO response = mlServiceIntegration.trainModel(request);

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

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
            @RequestBody(required = false) MLPredictionRequestDTO request,
            @AuthenticationPrincipal User currentUser) {

        try {
            if (request == null) {
                request = new MLPredictionRequestDTO();
            }

            if (request.getCreatedBy() == null && currentUser != null) {
                request.setCreatedBy(currentUser.getUsername());
            }

            if (request.getWeeksAhead() == null) {
                request.setWeeksAhead(4);
            }

            if (request.getSaveToDb() == null) {
                request.setSaveToDb(true);
            }

            MLPredictionResponseDTO response = mlServiceIntegration.generatePredictions(request);

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

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

        List<MLServicePredictionDTO> predictions =
                mlServiceIntegration.getPredictionsForDish(dishId, weeks);

        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/predictions/latest")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<List<MLServicePredictionDTO>> getLatestPredictions() {
        List<MLServicePredictionDTO> predictions =
                mlServiceIntegration.getLatestPredictions();

        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/model/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLModelInfoDTO> getActiveModelInfo() {
        MLModelInfoDTO info = mlServiceIntegration.getActiveModelInfo();
        return ResponseEntity.ok(info);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MLPredictionResponseDTO> syncPredictions(
            @RequestParam(defaultValue = "4") Integer weeks) {

        MLPredictionResponseDTO response =
                mlServiceIntegration.syncPredictionsToDatabase(weeks);

        if (Boolean.TRUE.equals(response.getSuccess())) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/quick-forecast/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<MLServicePredictionDTO> getQuickForecast(
            @PathVariable UUID dishId) {

        List<MLServicePredictionDTO> predictions =
                mlServiceIntegration.getPredictionsForDish(dishId, 1);

        if (predictions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(predictions.get(0));
    }

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