package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.PredictionDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.PredictionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ml/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<PredictionDTO> getAll() {
        return predictionService.getAll();
    }

    @GetMapping("/dish/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<PredictionDTO> getByDish(@PathVariable UUID dishId) {
        return predictionService.getByDish(dishId);
    }

    @GetMapping("/dish/{dishId}/range")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<PredictionDTO> getByDishAndRange(
            @PathVariable UUID dishId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return predictionService.getByDishAndDateRange(dishId, start, end);
    }

    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<PredictionDTO> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return predictionService.getByDateRange(start, end);
    }

    // 👉 Pensado para el script Python (enviar predicciones en lote)
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<PredictionDTO>> bulkCreate(
            @RequestBody List<PredictionDTO> dtos,
            @AuthenticationPrincipal User currentUser
    ) {
        List<PredictionDTO> created = predictionService.bulkCreate(dtos, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }
}
