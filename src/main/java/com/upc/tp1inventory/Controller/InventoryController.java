package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.InventoryDTO;
import com.upc.tp1inventory.Service.InventoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public InventoryDTO getByProduct(@PathVariable UUID productId) {
        return inventoryService.getByProductId(productId);
    }
}
