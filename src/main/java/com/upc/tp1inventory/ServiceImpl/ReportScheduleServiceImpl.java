package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.ReportScheduleDTO;
import com.upc.tp1inventory.Entity.ReportSchedule;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.ReportScheduleRepository;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.ReportScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportScheduleServiceImpl implements ReportScheduleService {

    private final ReportScheduleRepository reportScheduleRepository;
    private final UserRepository userRepository;

    public ReportScheduleServiceImpl(ReportScheduleRepository reportScheduleRepository,
                                     UserRepository userRepository) {
        this.reportScheduleRepository = reportScheduleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ReportScheduleDTO> getAll() {
        return reportScheduleRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportScheduleDTO> getActive() {
        return reportScheduleRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReportScheduleDTO getById(UUID id) {
        ReportSchedule rs = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de reporte no encontrada: " + id));
        return toDTO(rs);
    }

    @Override
    public ReportScheduleDTO create(ReportScheduleDTO dto, String username) {
        if (dto.getReportType() == null || dto.getReportType().isBlank()) {
            throw new IllegalArgumentException("reportType es obligatorio");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("title es obligatorio");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        ReportSchedule.ScheduleType scheduleType = null;
        if (dto.getScheduleType() != null) {
            try {
                scheduleType = ReportSchedule.ScheduleType.valueOf(dto.getScheduleType().toLowerCase());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("scheduleType inválido: " + dto.getScheduleType());
            }
        }

        ReportSchedule rs = new ReportSchedule();
        rs.setReportType(dto.getReportType());
        rs.setTitle(dto.getTitle());
        rs.setScheduleType(scheduleType);
        rs.setParameters(dto.getParameters());
        rs.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        rs.setLastGenerated(dto.getLastGenerated());
        rs.setNextGeneration(dto.getNextGeneration());
        rs.setCreatedBy(user);

        ReportSchedule saved = reportScheduleRepository.save(rs);
        return toDTO(saved);
    }

    @Override
    public ReportScheduleDTO update(UUID id, ReportScheduleDTO dto, String username) {
        ReportSchedule rs = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de reporte no encontrada: " + id));

        if (dto.getReportType() != null) {
            rs.setReportType(dto.getReportType());
        }
        if (dto.getTitle() != null) {
            rs.setTitle(dto.getTitle());
        }
        if (dto.getScheduleType() != null) {
            try {
                rs.setScheduleType(
                        ReportSchedule.ScheduleType.valueOf(dto.getScheduleType().toLowerCase())
                );
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("scheduleType inválido: " + dto.getScheduleType());
            }
        }
        if (dto.getParameters() != null) {
            rs.setParameters(dto.getParameters());
        }
        if (dto.getIsActive() != null) {
            rs.setIsActive(dto.getIsActive());
        }
        if (dto.getLastGenerated() != null) {
            rs.setLastGenerated(dto.getLastGenerated());
        }
        if (dto.getNextGeneration() != null) {
            rs.setNextGeneration(dto.getNextGeneration());
        }

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
            rs.setCreatedBy(user);
        }

        ReportSchedule saved = reportScheduleRepository.save(rs);
        return toDTO(saved);
    }

    @Override
    public ReportScheduleDTO activate(UUID id, boolean active) {
        ReportSchedule rs = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de reporte no encontrada: " + id));

        rs.setIsActive(active);
        ReportSchedule saved = reportScheduleRepository.save(rs);
        return toDTO(saved);
    }

    @Override
    public void delete(UUID id) {
        ReportSchedule rs = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programación de reporte no encontrada: " + id));
        reportScheduleRepository.delete(rs);
    }

    // ===== helper =====
    private ReportScheduleDTO toDTO(ReportSchedule rs) {
        ReportScheduleDTO dto = new ReportScheduleDTO();
        dto.setId(rs.getId());
        dto.setReportType(rs.getReportType());
        dto.setTitle(rs.getTitle());
        dto.setScheduleType(rs.getScheduleType() != null ? rs.getScheduleType().name() : null);
        dto.setParameters(rs.getParameters());
        dto.setIsActive(rs.getIsActive());
        dto.setLastGenerated(rs.getLastGenerated());
        dto.setNextGeneration(rs.getNextGeneration());
        if (rs.getCreatedBy() != null) {
            dto.setCreatedByUsername(rs.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(rs.getCreatedAt());
        return dto;
    }
}
