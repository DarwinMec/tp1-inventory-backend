package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.ReportScheduleDTO;

import java.util.List;
import java.util.UUID;

public interface ReportScheduleService {

    List<ReportScheduleDTO> getAll();

    List<ReportScheduleDTO> getActive();

    ReportScheduleDTO getById(UUID id);

    ReportScheduleDTO create(ReportScheduleDTO dto, String username);

    ReportScheduleDTO update(UUID id, ReportScheduleDTO dto, String username);

    ReportScheduleDTO activate(UUID id, boolean active);

    void delete(UUID id);
}
