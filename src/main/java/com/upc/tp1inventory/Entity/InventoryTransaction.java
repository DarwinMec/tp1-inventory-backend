package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

    public enum TransactionType {
        inbound, outbound, adjustment
    }

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_trans_product")
    )
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "supplier_id",
            foreignKey = @ForeignKey(name = "fk_inventory_trans_supplier")
    )
    private Supplier supplier;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_inventory_trans_created_by")
    )
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (transactionDate == null) transactionDate = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();

        if (totalCost == null && unitCost != null && quantity != null) {
            // total = unit_cost * quantity (ya decimal)
            this.totalCost = unitCost.multiply(quantity);
        }
    }
}
