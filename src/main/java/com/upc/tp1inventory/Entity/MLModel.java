package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ml_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLModel {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "model_name", length = 100, nullable = false)
    private String modelName;

    @Column(name = "model_type", length = 20)
    private String modelType; // Ej: "XGBoost"

    @Column(length = 20, nullable = false)
    private String version;

    // parameters jsonb en BD
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(precision = 5, scale = 4)
    private BigDecimal r2;

    @Column(precision = 10, scale = 4)
    private BigDecimal mae;

    @Column(precision = 10, scale = 4)
    private BigDecimal rmse;

    @Column(name = "trained_at")
    private LocalDateTime trainedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by",
            foreignKey = @ForeignKey(name = "fk_ml_models_created_by")
    )
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (modelType == null) modelType = "XGBoost";
        if (isActive == null) isActive = Boolean.TRUE;
    }
}
