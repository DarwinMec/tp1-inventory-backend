package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
}
