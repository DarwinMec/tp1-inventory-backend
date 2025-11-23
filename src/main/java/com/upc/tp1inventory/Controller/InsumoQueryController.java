package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.StockAlertaDto;
import com.upc.tp1inventory.Entity.Inventory;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Repository.InventoryRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/insumos")
@CrossOrigin(origins = "*")
public class InsumoQueryController {

    private final InventoryRepository inventoryRepository;

    public InsumoQueryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<StockAlertaDto> getAlertasStock() {
        List<Inventory> inventarios = inventoryRepository.findAll();

        return inventarios.stream()
                .filter(inv -> {
                    Product p = inv.getProduct();
                    if (p == null) return false;
                    Integer minStock = p.getMinStock();
                    if (minStock == null) return false;
                    if (inv.getCurrentStock() == null) return false;
                    return inv.getCurrentStock()
                            .compareTo(BigDecimal.valueOf(minStock)) <= 0;
                })
                .map(inv -> {
                    Product p = inv.getProduct();
                    StockAlertaDto dto = new StockAlertaDto();
                    dto.setId(p.getId());
                    dto.setNombre(p.getName());
                    dto.setCategoria(
                            p.getCategory() != null ? p.getCategory().getName() : "Sin categoría"
                    );
                    dto.setStockActual(
                            inv.getCurrentStock() != null
                                    ? inv.getCurrentStock().intValue()
                                    : 0
                    );
                    dto.setStockMinimo(
                            Objects.requireNonNullElse(p.getMinStock(), 0)
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
