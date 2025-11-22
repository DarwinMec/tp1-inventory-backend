package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.SeasonalFactorDTO;
import com.upc.tp1inventory.Entity.SeasonalFactor;
import com.upc.tp1inventory.Repository.SeasonalFactorRepository;
import com.upc.tp1inventory.Service.SeasonalFactorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeasonalFactorServiceImpl implements SeasonalFactorService {

    private final SeasonalFactorRepository seasonalFactorRepository;

    public SeasonalFactorServiceImpl(SeasonalFactorRepository seasonalFactorRepository) {
        this.seasonalFactorRepository = seasonalFactorRepository;
    }

    @Override
    public List<SeasonalFactorDTO> getAll() {
        return seasonalFactorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeasonalFactorDTO> getByMonth(int month) {
        return seasonalFactorRepository.findByMonthOrderByDayOfWeekAsc(month)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SeasonalFactorDTO> bulkUpsert(List<SeasonalFactorDTO> dtos) {
        // Validaciones simples
        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("La lista de factores estacionales no puede estar vacía");
        }

        List<SeasonalFactor> saved = dtos.stream()
                .map(dto -> {
                    if (dto.getMonth() == null || dto.getDayOfWeek() == null) {
                        throw new IllegalArgumentException("Cada registro debe tener month y dayOfWeek");
                    }

                    return seasonalFactorRepository
                            .findByMonthAndDayOfWeek(dto.getMonth(), dto.getDayOfWeek())
                            .map(existing -> updateEntityFromDTO(existing, dto))
                            .orElseGet(() -> createEntityFromDTO(dto));
                })
                .map(seasonalFactorRepository::save)
                .collect(Collectors.toList());

        return saved.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // =================== helpers ===================

    private SeasonalFactor updateEntityFromDTO(SeasonalFactor entity, SeasonalFactorDTO dto) {
        if (dto.getFactor() != null) {
            entity.setFactor(dto.getFactor());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        return entity;
    }

    private SeasonalFactor createEntityFromDTO(SeasonalFactorDTO dto) {
        return SeasonalFactor.builder()
                .month(dto.getMonth())
                .dayOfWeek(dto.getDayOfWeek())
                .factor(dto.getFactor() != null ? dto.getFactor() : BigDecimal.valueOf(1.0))
                .description(dto.getDescription())
                .build();
    }

    private SeasonalFactorDTO toDTO(SeasonalFactor entity) {
        SeasonalFactorDTO dto = new SeasonalFactorDTO();
        dto.setId(entity.getId());
        dto.setMonth(entity.getMonth());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setFactor(entity.getFactor());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
