package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByTableNameOrderByCreatedAtDesc(String tableName);

    List<AuditLog> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}