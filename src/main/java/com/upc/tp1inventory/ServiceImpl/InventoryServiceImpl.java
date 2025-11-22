package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.InventoryDTO;
import com.upc.tp1inventory.Entity.Inventory;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.InventoryRepository;
import com.upc.tp1inventory.Repository.ProductRepository;
import com.upc.tp1inventory.Service.AlertService;
import com.upc.tp1inventory.Service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final AlertService alertService;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                ProductRepository productRepository,
                                AlertService alertService) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.alertService = alertService;
    }

    @Override
    public InventoryDTO getByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado: " + productId));

        // Si no existe inventario, considerar stock 0 (como antes)
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseGet(() -> Inventory.builder()
                        .product(product)
                        .currentStock(BigDecimal.ZERO)
                        .availableStock(BigDecimal.ZERO)
                        .reservedStock(BigDecimal.ZERO)
                        .build());

        return toDTO(inventory);
    }

    @Override
    public boolean hasSufficientStock(UUID productId, double requiredQuantity) {

        Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                .orElse(null);

        if (inventory == null || inventory.getCurrentStock() == null) {
            return false;
        }

        BigDecimal current = inventory.getCurrentStock();
        BigDecimal required = BigDecimal.valueOf(requiredQuantity);

        return current.compareTo(required) >= 0;
    }

    @Override
    public void deductStock(UUID productId, double quantityToDeduct) {

        Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventario no encontrado para producto: " + productId));

        BigDecimal current = inventory.getCurrentStock() != null
                ? inventory.getCurrentStock()
                : BigDecimal.ZERO;

        BigDecimal available = inventory.getAvailableStock() != null
                ? inventory.getAvailableStock()
                : BigDecimal.ZERO;

        BigDecimal toDeduct = BigDecimal.valueOf(quantityToDeduct);

        if (current.compareTo(toDeduct) < 0) {
            throw new IllegalArgumentException(
                    "Stock insuficiente para producto " + productId +
                            ". Disponible: " + current + ", requerido: " + toDeduct
            );
        }

        inventory.setCurrentStock(current.subtract(toDeduct));
        inventory.setAvailableStock(available.subtract(toDeduct));

        inventoryRepository.save(inventory);
        // ✅ Después de actualizar stock, revisar si debe generarse alerta
        alertService.createLowStockAlert(inventory.getProduct(), inventory.getCurrentStock());
    }

    InventoryDTO toDTO(Inventory inv) {
        InventoryDTO dto = new InventoryDTO();

        dto.setProductId(inv.getProduct().getId());
        dto.setProductName(inv.getProduct().getName());
        dto.setCurrentStock(inv.getCurrentStock());
        dto.setAvailableStock(inv.getAvailableStock());
        dto.setReservedStock(inv.getReservedStock());
        dto.setLastUpdated(inv.getLastUpdated());

        return dto;
    }
}
