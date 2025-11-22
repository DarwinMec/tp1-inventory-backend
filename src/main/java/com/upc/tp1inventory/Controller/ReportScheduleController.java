package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.ReportScheduleDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.ReportScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/report-schedules")
@CrossOrigin(origins = "*")
public class ReportScheduleController {

    private final ReportScheduleService reportScheduleService;

    public ReportScheduleController(ReportScheduleService reportScheduleService) {
        this.reportScheduleService = reportScheduleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ReportScheduleDTO> getAll() {
        return reportScheduleService.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ReportScheduleDTO> getActive() {
        return reportScheduleService.getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ReportScheduleDTO getById(@PathVariable UUID id) {
        return reportScheduleService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportScheduleDTO> create(
            @RequestBody ReportScheduleDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        ReportScheduleDTO created = reportScheduleService.create(dto, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportScheduleDTO> update(
            @PathVariable UUID id,
            @RequestBody ReportScheduleDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        ReportScheduleDTO updated = reportScheduleService.update(id, dto, currentUser.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportScheduleDTO> setActive(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        ReportScheduleDTO updated = reportScheduleService.activate(id, active);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reportScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
