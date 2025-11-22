package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.InventoryDTO;

import java.util.UUID;

public interface InventoryService {

    InventoryDTO getByProductId(UUID productId);

    // ✅ Nuevo: validar si hay stock suficiente
    boolean hasSufficientStock(UUID productId, double requiredQuantity);

    // ✅ Nuevo: descontar stock cuando la venta se confirma
    void deductStock(UUID productId, double quantityToDeduct);
}
