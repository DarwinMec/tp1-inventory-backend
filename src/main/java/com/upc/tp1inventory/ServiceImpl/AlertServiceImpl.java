package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.AlertDTO;
import com.upc.tp1inventory.Entity.Alert;
import com.upc.tp1inventory.Entity.AlertType;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.AlertRepository;
import com.upc.tp1inventory.Repository.AlertTypeRepository;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AlertTypeRepository alertTypeRepository;
    private final UserRepository userRepository;

    private static final String LOW_STOCK_TYPE = "LOW_STOCK";

    public AlertServiceImpl(AlertRepository alertRepository,
                            AlertTypeRepository alertTypeRepository,
                            UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.alertTypeRepository = alertTypeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<AlertDTO> getAlerts(Boolean unreadOnly, Boolean unresolvedOnly) {
        List<Alert> alerts;

        if (Boolean.TRUE.equals(unreadOnly)) {
            alerts = alertRepository.findByIsReadFalseOrderByCreatedAtDesc();
        } else if (Boolean.TRUE.equals(unresolvedOnly)) {
            alerts = alertRepository.findByIsResolvedFalseOrderByCreatedAtDesc();
        } else {
            alerts = alertRepository.findByIsReadFalseOrIsResolvedFalseOrderByCreatedAtDesc();
        }

        return alerts.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public AlertDTO markAsRead(UUID alertId, String username) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + alertId));

        alert.setIsRead(true);
        return toDTO(alertRepository.save(alert));
    }

    @Override
    public AlertDTO markAsResolved(UUID alertId, String username) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + alertId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        alert.setIsResolved(true);
        alert.setIsRead(true);
        alert.setResolvedBy(user);
        alert.setResolvedAt(LocalDateTime.now());

        return toDTO(alertRepository.save(alert));
    }

    @Override
    public void createLowStockAlert(Product product, BigDecimal currentStock) {

        if (product == null || product.getId() == null) return;

        // Evitar alertas duplicadas no resueltas para el mismo producto
        if (alertRepository.existsByAlertType_NameAndProduct_IdAndIsResolvedFalse(LOW_STOCK_TYPE, product.getId())) {
            return;
        }

        Integer minStock = product.getMinStock();
        Integer reorderPoint = product.getReorderPoint();

        // Determinar severidad
        String severity = "low";

        if (minStock != null && currentStock.compareTo(BigDecimal.valueOf(minStock)) <= 0) {
            severity = "high";
        } else if (reorderPoint != null && currentStock.compareTo(BigDecimal.valueOf(reorderPoint)) <= 0) {
            severity = "medium";
        } else {
            // Si ni siquiera llegó al reorderPoint, no generamos alerta
            return;
        }

        AlertType type = getOrCreateAlertType(
                LOW_STOCK_TYPE,
                "Alerta automática de bajo stock para productos por debajo del punto de reorden o stock mínimo.",
                severity
        );

        String title = "Stock bajo de " + product.getName();
        String message = "El producto '" + product.getName() +
                "' tiene un stock actual de " + currentStock +
                " " + product.getUnitMeasure() +
                ". Revise y considere generar una orden de compra.";

        Alert alert = Alert.builder()
                .alertType(type)
                .title(title)
                .message(message)
                .severity(severity)
                .product(product)
                .isRead(false)
                .isResolved(false)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
    }

    private AlertType getOrCreateAlertType(String name, String description, String severity) {
        return alertTypeRepository.findByName(name)
                .map(existing -> {
                    if (existing.getSeverity() == null) {
                        existing.setSeverity(severity);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    AlertType t = new AlertType();
                    t.setName(name);
                    t.setDescription(description);
                    t.setSeverity(severity);
                    t.setIsActive(true);
                    return alertTypeRepository.save(t);
                });
    }

    private AlertDTO toDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();

        dto.setId(alert.getId());

        if (alert.getAlertType() != null) {
            dto.setAlertTypeId(alert.getAlertType().getId());
            dto.setAlertTypeName(alert.getAlertType().getName());
        }

        dto.setTitle(alert.getTitle());
        dto.setMessage(alert.getMessage());
        dto.setSeverity(alert.getSeverity());

        if (alert.getProduct() != null) {
            dto.setProductId(alert.getProduct().getId());
            dto.setProductName(alert.getProduct().getName());
        }

        if (alert.getDish() != null) {
            dto.setDishId(alert.getDish().getId());
            dto.setDishName(alert.getDish().getName());
        }

        dto.setIsRead(alert.getIsRead());
        dto.setIsResolved(alert.getIsResolved());

        if (alert.getResolvedBy() != null) {
            dto.setResolvedByUsername(alert.getResolvedBy().getUsername());
        }
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setCreatedAt(alert.getCreatedAt());

        return dto;
    }
}
