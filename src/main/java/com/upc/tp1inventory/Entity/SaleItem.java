package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sale_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sale_items_sale")
    )
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dish_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sale_items_dish")
    )
    private Dish dish;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 8, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;
}
