package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.SystemSettingDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.SystemSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system-settings")
@CrossOrigin(origins = "*")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    public SystemSettingController(SystemSettingService systemSettingService) {
        this.systemSettingService = systemSettingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SystemSettingDTO> getAll() {
        return systemSettingService.getAll();
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public SystemSettingDTO getByKey(@PathVariable String key) {
        return systemSettingService.getByKey(key);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingDTO> create(
            @RequestBody SystemSettingDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        SystemSettingDTO created = systemSettingService.create(dto, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingDTO> update(
            @PathVariable String key,
            @RequestBody SystemSettingDTO dto,
            @AuthenticationPrincipal User currentUser
    ) {
        SystemSettingDTO updated = systemSettingService.update(key, dto, currentUser.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        systemSettingService.delete(key);
        return ResponseEntity.noContent().build();
    }
}
