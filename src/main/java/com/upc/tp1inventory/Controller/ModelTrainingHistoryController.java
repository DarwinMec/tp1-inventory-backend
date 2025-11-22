package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.ModelTrainingHistoryDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.ModelTrainingHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ml/training-history")
@CrossOrigin(origins = "*")
public class ModelTrainingHistoryController {

    private final ModelTrainingHistoryService historyService;

    public ModelTrainingHistoryController(ModelTrainingHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/model/{modelId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ModelTrainingHistoryDTO> getByModel(@PathVariable UUID modelId) {
        return historyService.getByModel(modelId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ModelTrainingHistoryDTO> create(@RequestBody ModelTrainingHistoryDTO dto,
                                                          @AuthenticationPrincipal User currentUser) {
        ModelTrainingHistoryDTO created = historyService.create(dto, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }
}
