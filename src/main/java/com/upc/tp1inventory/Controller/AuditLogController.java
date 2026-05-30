package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.AuditLogDTO;
import com.upc.tp1inventory.Service.AuditLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogDTO> getAll() {
        return auditLogService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AuditLogDTO getById(@PathVariable UUID id) {
        return auditLogService.getById(id);
    }

    @GetMapping("/table/{tableName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogDTO> getByTable(@PathVariable String tableName) {
        return auditLogService.getByTable(tableName);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogDTO> getByUser(@PathVariable UUID userId) {
        return auditLogService.getByUser(userId);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogDTO> getByAction(@PathVariable String action) {
        return auditLogService.getByAction(action);
    }
}