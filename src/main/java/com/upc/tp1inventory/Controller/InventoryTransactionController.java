package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.InventoryTransactionDTO;
import com.upc.tp1inventory.Service.InventoryTransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.upc.tp1inventory.Entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/transactions")
@CrossOrigin(origins = "*")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    public InventoryTransactionController(InventoryTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<InventoryTransactionDTO> getAll() {
        return transactionService.getAll();
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<InventoryTransactionDTO> getByProduct(@PathVariable UUID productId) {
        return transactionService.getByProduct(productId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<InventoryTransactionDTO> create(@RequestBody InventoryTransactionDTO dto,
                                                          @AuthenticationPrincipal User currentUser) {
        InventoryTransactionDTO created = transactionService.create(dto, currentUser.getUsername());
        return ResponseEntity.ok(created);
    }
}
