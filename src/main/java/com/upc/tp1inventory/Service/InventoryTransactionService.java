package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.InventoryTransactionDTO;

import java.util.List;
import java.util.UUID;

public interface InventoryTransactionService {

    List<InventoryTransactionDTO> getAll();

    List<InventoryTransactionDTO> getByProduct(UUID productId);

    InventoryTransactionDTO create(InventoryTransactionDTO dto, String username);
}
