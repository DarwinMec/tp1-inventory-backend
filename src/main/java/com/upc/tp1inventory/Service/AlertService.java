package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.AlertDTO;
import com.upc.tp1inventory.Entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AlertService {

    // Listar alertas con filtros simples
    List<AlertDTO> getAlerts(Boolean unreadOnly, Boolean unresolvedOnly);

    AlertDTO markAsRead(UUID alertId, String username);

    AlertDTO markAsResolved(UUID alertId, String username);

    // Generada automáticamente cuando baja el stock
    void createLowStockAlert(Product product, BigDecimal currentStock);
}
