package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.InventoryTransactionDTO;
import com.upc.tp1inventory.Entity.*;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.*;
import com.upc.tp1inventory.Service.InventoryTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    public InventoryTransactionServiceImpl(InventoryTransactionRepository transactionRepository,
                                           InventoryRepository inventoryRepository,
                                           ProductRepository productRepository,
                                           SupplierRepository supplierRepository,
                                           UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<InventoryTransactionDTO> getAll() {
        return transactionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryTransactionDTO> getByProduct(UUID productId) {
        return transactionRepository.findByProduct_IdOrderByTransactionDateDesc(productId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryTransactionDTO create(InventoryTransactionDTO dto, String username) {

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado: " + dto.getProductId()));

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Proveedor no encontrado: " + dto.getSupplierId()));
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        InventoryTransaction.TransactionType type;
        try {
            type = InventoryTransaction.TransactionType.valueOf(dto.getTransactionType().toLowerCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo de transacción inválido: " + dto.getTransactionType());
        }


        BigDecimal quantity = dto.getQuantity() != null
                ? dto.getQuantity()
                : BigDecimal.ZERO;

        InventoryTransaction tx = new InventoryTransaction();
        tx.setProduct(product);
        tx.setTransactionType(type);
        tx.setQuantity(quantity);
        tx.setUnitCost(dto.getUnitCost());

        if (dto.getTotalCost() != null) {
            tx.setTotalCost(dto.getTotalCost());
        } else if (dto.getUnitCost() != null) {
            tx.setTotalCost(dto.getUnitCost().multiply(quantity));
        }

        tx.setSupplier(supplier);
        tx.setReferenceNumber(dto.getReferenceNumber());
        tx.setNotes(dto.getNotes());
        tx.setTransactionDate(dto.getTransactionDate());
        tx.setCreatedBy(user);

        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setCurrentStock(BigDecimal.ZERO);
                    inv.setAvailableStock(BigDecimal.ZERO);
                    inv.setReservedStock(BigDecimal.ZERO);
                    return inv;
                });

        BigDecimal current = inventory.getCurrentStock() != null
                ? inventory.getCurrentStock()
                : BigDecimal.ZERO;

        BigDecimal available = inventory.getAvailableStock() != null
                ? inventory.getAvailableStock()
                : BigDecimal.ZERO;


        switch (type) {

            case inbound -> {
                inventory.setCurrentStock(current.add(quantity));
                inventory.setAvailableStock(available.add(quantity));
            }

            case outbound -> {
                // Validar stock disponible
                if (current.compareTo(quantity) < 0) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente para producto " + product.getName() +
                                    ". Disponible: " + current + ", requerido: " + quantity
                    );
                }
                inventory.setCurrentStock(current.subtract(quantity));
                inventory.setAvailableStock(available.subtract(quantity));
            }

            case adjustment -> {
                // ajuste directo (positivo o negativo)
                inventory.setCurrentStock(current.add(quantity));
                inventory.setAvailableStock(available.add(quantity));
            }
        }

        inventory.setUpdatedBy(user.getId());
        inventoryRepository.save(inventory);

        InventoryTransaction saved = transactionRepository.save(tx);

        return toDTO(saved);
    }

    private InventoryTransactionDTO toDTO(InventoryTransaction tx) {

        InventoryTransactionDTO dto = new InventoryTransactionDTO();

        dto.setId(tx.getId());
        dto.setProductId(tx.getProduct().getId());
        dto.setProductName(tx.getProduct().getName());
        dto.setTransactionType(tx.getTransactionType().name());
        dto.setQuantity(tx.getQuantity());
        dto.setUnitCost(tx.getUnitCost());
        dto.setTotalCost(tx.getTotalCost());

        if (tx.getSupplier() != null) {
            dto.setSupplierId(tx.getSupplier().getId());
            dto.setSupplierName(tx.getSupplier().getName());
        }

        dto.setReferenceNumber(tx.getReferenceNumber());
        dto.setNotes(tx.getNotes());
        dto.setTransactionDate(tx.getTransactionDate());

        return dto;
    }
}
