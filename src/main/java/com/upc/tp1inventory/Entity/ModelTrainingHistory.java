package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "model_training_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelTrainingHistory {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "model_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_training_history_model")
    )
    private MLModel model;

    @Column(name = "training_start", nullable = false)
    private LocalDateTime trainingStart;

    @Column(name = "training_end")
    private LocalDateTime trainingEnd;

    @Column(name = "data_points_used")
    private Integer dataPointsUsed;

    @Column(name = "r2Before", precision = 5, scale = 4)
    private BigDecimal r2Before;

    @Column(name = "r2After", precision = 5, scale = 4)
    private BigDecimal r2After;

    @Column(length = 20)
    private String status; // training, completed, failed

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_training_history_created_by")
    )
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "completed";
    }
}
