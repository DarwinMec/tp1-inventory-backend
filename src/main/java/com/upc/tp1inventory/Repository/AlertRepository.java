package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findByIsReadFalseOrIsResolvedFalseOrderByCreatedAtDesc();

    List<Alert> findByIsReadFalseOrderByCreatedAtDesc();

    List<Alert> findByIsResolvedFalseOrderByCreatedAtDesc();

    boolean existsByAlertType_NameAndProduct_IdAndIsResolvedFalse(String alertTypeName, UUID productId);
}
