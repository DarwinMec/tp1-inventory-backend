package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_product")
    )
    private Product product;

    @Column(name = "current_stock", nullable = false)
    private BigDecimal currentStock ;

    @Column(name = "available_stock", nullable = false)
    private BigDecimal availableStock ;

    @Column(name = "reserved_stock", nullable = false)
    private BigDecimal reservedStock ;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "updated_by", columnDefinition = "uuid")
    private UUID updatedBy;

    @PrePersist
    public void prePersist() {
        if (currentStock == null) currentStock = BigDecimal.ZERO;
        if (availableStock == null) availableStock = BigDecimal.ZERO;
        if (reservedStock == null) reservedStock = BigDecimal.ZERO;
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
