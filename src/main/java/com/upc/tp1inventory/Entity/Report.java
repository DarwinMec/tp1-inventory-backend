package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "report_type", length = 50, nullable = false)
    private String reportType; // Ej: SALES_SUMMARY

    @Column(length = 200, nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_format", length = 10)
    private String fileFormat; // Ej: "JSON", "CSV", "PDF" (por ahora usaremos "JSON")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "generated_by",
            foreignKey = @ForeignKey(name = "fk_reports_generated_by")
    )
    private User generatedBy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
