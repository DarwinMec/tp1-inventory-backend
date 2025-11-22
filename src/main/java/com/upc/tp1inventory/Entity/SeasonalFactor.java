package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "seasonal_factors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalFactor {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "month", nullable = false)
    private Integer month;  // 1..12

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1..7

    @Column(name = "factor", precision = 4, scale = 2)
    private BigDecimal factor;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (factor == null) factor = BigDecimal.valueOf(1.0);
    }
}
