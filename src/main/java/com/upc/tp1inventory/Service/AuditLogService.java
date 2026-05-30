package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.AuditLogDTO;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {

    List<AuditLogDTO> getAll();

    List<AuditLogDTO> getByTable(String tableName);

    List<AuditLogDTO> getByUser(UUID userId);

    List<AuditLogDTO> getByAction(String action);

    AuditLogDTO getById(UUID id);
}