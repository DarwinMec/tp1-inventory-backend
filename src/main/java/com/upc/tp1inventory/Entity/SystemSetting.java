package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "setting_key", length = 100, nullable = false, unique = true)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "text")
    private String settingValue;

    @Column(name = "data_type", length = 20)
    private String dataType; // string, int, double, boolean, json, etc.

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_by",
            foreignKey = @ForeignKey(name = "fk_system_settings_updated_by")
    )
    private User updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void preSave() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        } else {
            updatedAt = LocalDateTime.now();
        }
        if (dataType == null) {
            dataType = "string";
        }
    }
}
