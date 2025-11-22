package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "alert_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertType {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 50, nullable = false, unique = true)
    private String name; // Ej: "LOW_STOCK"

    @Column(columnDefinition = "text")
    private String description;

    // low, medium, high, critical (como en el CHECK de la BD)
    @Column(length = 10)
    private String severity;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
