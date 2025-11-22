package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.SaleDTO;
import com.upc.tp1inventory.Service.SaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Ver todas las ventas (admin, manager, employee)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<SaleDTO> getAll() {
        return saleService.getAll();
    }

    // Ver una venta por id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public SaleDTO getById(@PathVariable UUID id) {
        return saleService.getById(id);
    }

    // Crear una venta (por ahora la permitimos a todos los roles autenticados)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public ResponseEntity<SaleDTO> create(@RequestBody SaleDTO dto) {
        SaleDTO created = saleService.create(dto);
        return ResponseEntity.ok(created);
    }
}
