package com.upc.tp1inventory.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "text")
    private String address;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String region;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (city == null) city = "Chiclayo";
        if (region == null) region = "Lambayeque";
        if (isActive == null) isActive = true;
    }
}
