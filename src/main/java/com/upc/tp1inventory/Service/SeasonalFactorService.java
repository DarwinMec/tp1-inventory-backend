package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.SeasonalFactorDTO;

import java.util.List;

public interface SeasonalFactorService {

    List<SeasonalFactorDTO> getAll();

    List<SeasonalFactorDTO> getByMonth(int month);

    List<SeasonalFactorDTO> bulkUpsert(List<SeasonalFactorDTO> dtos);
}
