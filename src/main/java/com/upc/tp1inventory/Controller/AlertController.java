package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.AlertDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // Listar alertas (puede filtrar por unread / unresolved)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<AlertDTO> getAlerts(
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) Boolean unresolvedOnly) {

        return alertService.getAlerts(unreadOnly, unresolvedOnly);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<AlertDTO> markAsRead(@PathVariable UUID id,
                                               @AuthenticationPrincipal User currentUser) {
        AlertDTO updated = alertService.markAsRead(id, currentUser.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AlertDTO> markAsResolved(@PathVariable UUID id,
                                                   @AuthenticationPrincipal User currentUser) {
        AlertDTO updated = alertService.markAsResolved(id, currentUser.getUsername());
        return ResponseEntity.ok(updated);
    }
}
