package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "alert_type_id",
            foreignKey = @ForeignKey(name = "fk_alerts_type")
    )
    private AlertType alertType;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "text", nullable = false)
    private String message;

    // low, medium, high, critical
    @Column(length = 10)
    private String severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            foreignKey = @ForeignKey(name = "fk_alerts_product")
    )
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dish_id",
            foreignKey = @ForeignKey(name = "fk_alerts_dish")
    )
    private Dish dish;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "resolved_by",
            foreignKey = @ForeignKey(name = "fk_alerts_resolved_by")
    )
    private User resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
        if (isResolved == null) isResolved = false;
    }
}
