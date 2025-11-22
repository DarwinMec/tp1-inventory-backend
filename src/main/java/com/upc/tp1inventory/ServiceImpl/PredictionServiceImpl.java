package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.PredictionDTO;
import com.upc.tp1inventory.Entity.*;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.*;
import com.upc.tp1inventory.Service.PredictionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PredictionServiceImpl implements PredictionService {

    private final PredictionRepository predictionRepository;
    private final MLModelRepository mlModelRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;

    public PredictionServiceImpl(PredictionRepository predictionRepository,
                                 MLModelRepository mlModelRepository,
                                 DishRepository dishRepository,
                                 UserRepository userRepository) {
        this.predictionRepository = predictionRepository;
        this.mlModelRepository = mlModelRepository;
        this.dishRepository = dishRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<PredictionDTO> getAll() {
        return predictionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionDTO> getByDish(UUID dishId) {
        return predictionRepository.findByDish_IdOrderByPredictedDateAsc(dishId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionDTO> getByDishAndDateRange(UUID dishId, LocalDate start, LocalDate end) {
        return predictionRepository
                .findByDish_IdAndPredictedDateBetweenOrderByPredictedDateAsc(dishId, start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionDTO> getByDateRange(LocalDate start, LocalDate end) {
        return predictionRepository
                .findByPredictedDateBetweenOrderByPredictedDateAsc(start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PredictionDTO> bulkCreate(List<PredictionDTO> dtos, String username) {

        if (dtos == null || dtos.isEmpty()) {
            throw new IllegalArgumentException("La lista de predicciones no puede estar vacía");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        List<Prediction> entities = dtos.stream()
                .map(dto -> {

                    if (dto.getModelId() == null) {
                        throw new IllegalArgumentException("Cada predicción debe incluir modelId");
                    }
                    if (dto.getDishId() == null) {
                        throw new IllegalArgumentException("Cada predicción debe incluir dishId");
                    }
                    if (dto.getPredictedDate() == null) {
                        throw new IllegalArgumentException("Cada predicción debe incluir predictedDate");
                    }
                    if (dto.getPredictedQuantity() == null) {
                        throw new IllegalArgumentException("Cada predicción debe incluir predictedQuantity");
                    }

                    MLModel model = mlModelRepository.findById(dto.getModelId())
                            .orElseThrow(() -> new ResourceNotFoundException("Modelo ML no encontrado: " + dto.getModelId()));

                    Dish dish = dishRepository.findById(dto.getDishId())
                            .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + dto.getDishId()));

                    Prediction p = new Prediction();
                    p.setModel(model);
                    p.setDish(dish);
                    p.setPredictedDate(dto.getPredictedDate());
                    p.setPredictedQuantity(dto.getPredictedQuantity());
                    p.setConfidenceLevel(dto.getConfidenceLevel());
                    p.setWeatherFactor(dto.getWeatherFactor());
                    p.setSeasonalFactor(dto.getSeasonalFactor());
                    p.setTrendFactor(dto.getTrendFactor());
                    p.setCreatedBy(user);

                    return p;
                })
                .collect(Collectors.toList());

        List<Prediction> saved = predictionRepository.saveAll(entities);

        return saved.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ================= helpers =================

    private PredictionDTO toDTO(Prediction p) {
        PredictionDTO dto = new PredictionDTO();
        dto.setId(p.getId());

        if (p.getModel() != null) {
            dto.setModelId(p.getModel().getId());
            dto.setModelName(p.getModel().getModelName());
        }

        if (p.getDish() != null) {
            dto.setDishId(p.getDish().getId());
            dto.setDishName(p.getDish().getName());
        }

        dto.setPredictedDate(p.getPredictedDate());
        dto.setPredictedQuantity(p.getPredictedQuantity());
        dto.setConfidenceLevel(p.getConfidenceLevel());
        dto.setWeatherFactor(p.getWeatherFactor());
        dto.setSeasonalFactor(p.getSeasonalFactor());
        dto.setTrendFactor(p.getTrendFactor());

        if (p.getCreatedBy() != null) {
            dto.setCreatedByUsername(p.getCreatedBy().getUsername());
        }
        dto.setCreatedAt(p.getCreatedAt());

        return dto;
    }
}
