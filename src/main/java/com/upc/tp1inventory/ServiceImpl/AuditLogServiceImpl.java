package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.AuditLogDTO;
import com.upc.tp1inventory.Entity.AuditLog;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.AuditLogRepository;
import com.upc.tp1inventory.Service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<AuditLogDTO> getAll() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getByTable(String tableName) {
        return auditLogRepository.findByTableNameOrderByCreatedAtDesc(tableName)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getByUser(UUID userId) {
        return auditLogRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action.toUpperCase())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AuditLogDTO getById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de auditoría no encontrado: " + id));
        return toDTO(auditLog);
    }

    private AuditLogDTO toDTO(AuditLog auditLog) {
        AuditLogDTO dto = new AuditLogDTO();

        dto.setId(auditLog.getId());

        if (auditLog.getUser() != null) {
            dto.setUserId(auditLog.getUser().getId());
            dto.setUsername(auditLog.getUser().getUsername());
        }

        dto.setTableName(auditLog.getTableName());
        dto.setRecordId(auditLog.getRecordId());
        dto.setAction(auditLog.getAction());
        dto.setOldValues(auditLog.getOldValues());
        dto.setNewValues(auditLog.getNewValues());
        dto.setIpAddress(auditLog.getIpAddress());
        dto.setUserAgent(auditLog.getUserAgent());
        dto.setCreatedAt(auditLog.getCreatedAt());

        return dto;
    }
}