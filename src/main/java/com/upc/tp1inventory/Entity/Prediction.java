package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "model_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_predictions_model")
    )
    private MLModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dish_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_predictions_dish")
    )
    private Dish dish;

    @Column(name = "predicted_date", nullable = false)
    private LocalDate predictedDate;

    @Column(name = "predicted_quantity", nullable = false)
    private Integer predictedQuantity;

    @Column(name = "confidence_level", precision = 5, scale = 4)
    private BigDecimal confidenceLevel;

    @Column(name = "weather_factor", length = 20)
    private String weatherFactor;

    @Column(name = "seasonal_factor", precision = 4, scale = 2)
    private BigDecimal seasonalFactor;

    @Column(name = "trend_factor", precision = 4, scale = 2)
    private BigDecimal trendFactor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_predictions_created_by")
    )
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
