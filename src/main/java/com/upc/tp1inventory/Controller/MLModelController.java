package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.MLModelDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.MLModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ml/models")
@CrossOrigin(origins = "*")
public class MLModelController {

    private final MLModelService mlModelService;

    public MLModelController(MLModelService mlModelService) {
        this.mlModelService = mlModelService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<MLModelDTO> getAll() {
        return mlModelService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MLModelDTO getById(@PathVariable UUID id) {
        return mlModelService.getById(id);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public MLModelDTO getActiveByName(@RequestParam String modelName) {
        return mlModelService.getActiveByName(modelName);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLModelDTO> create(@RequestBody MLModelDTO dto,
                                             @AuthenticationPrincipal User currentUser) {
        MLModelDTO created = mlModelService.create(dto, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLModelDTO> update(@PathVariable UUID id,
                                             @RequestBody MLModelDTO dto) {
        MLModelDTO updated = mlModelService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MLModelDTO> setActive(@PathVariable UUID id,
                                                @RequestParam boolean active) {
        MLModelDTO updated = mlModelService.setActive(id, active);
        return ResponseEntity.ok(updated);
    }
}
