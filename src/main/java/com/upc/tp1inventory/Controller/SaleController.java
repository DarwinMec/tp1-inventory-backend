package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.SaleDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Service.SaleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<SaleDTO> getAll() {
        return saleService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public SaleDTO getById(@PathVariable UUID id) {
        return saleService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<SaleDTO> create(@RequestBody SaleDTO dto,
                                          @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            throw new IllegalArgumentException("No se encontró el usuario autenticado");
        }

        SaleDTO created = saleService.create(dto, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<Page<SaleDTO>> getAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(saleService.getAllPaginated(page, size));
    }
}