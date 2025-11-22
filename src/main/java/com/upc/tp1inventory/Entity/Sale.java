package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(name = "sale_time", nullable = false)
    private LocalTime saleTime;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year")
    private Integer year;

    @Column(name = "weather", length = 20)
    private String weather;

    @Column(name = "is_holiday")
    private Boolean isHoliday = false;

    @Column(name = "is_weekend")
    private Boolean isWeekend = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (saleDate == null) {
            saleDate = now.toLocalDate();
        }
        if (saleTime == null) {
            saleTime = now.toLocalTime();
        }
        if (createdAt == null) {
            createdAt = now;
        }

        if (dayOfWeek == null) {
            // Lunes=1 ... Domingo=7 (como en tu diseño original)
            this.dayOfWeek = saleDate.getDayOfWeek().getValue();
        }
        if (month == null) {
            this.month = saleDate.getMonthValue();
        }
        if (year == null) {
            this.year = saleDate.getYear();
        }

        if (isWeekend == null) {
            int dow = saleDate.getDayOfWeek().getValue();
            // Sábado=6, Domingo=7
            this.isWeekend = (dow == 6 || dow == 7);
        }
        if (isHoliday == null) {
            this.isHoliday = false;
        }
    }
}
