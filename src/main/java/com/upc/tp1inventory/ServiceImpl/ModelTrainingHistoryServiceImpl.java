package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.ModelTrainingHistoryDTO;
import com.upc.tp1inventory.Entity.MLModel;
import com.upc.tp1inventory.Entity.ModelTrainingHistory;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.MLModelRepository;
import com.upc.tp1inventory.Repository.ModelTrainingHistoryRepository;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.ModelTrainingHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModelTrainingHistoryServiceImpl implements ModelTrainingHistoryService {

    private final ModelTrainingHistoryRepository historyRepository;
    private final MLModelRepository mlModelRepository;
    private final UserRepository userRepository;

    public ModelTrainingHistoryServiceImpl(ModelTrainingHistoryRepository historyRepository,
                                           MLModelRepository mlModelRepository,
                                           UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.mlModelRepository = mlModelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ModelTrainingHistoryDTO> getByModel(UUID modelId) {
        MLModel model = mlModelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo ML no encontrado: " + modelId));

        return historyRepository.findByModelOrderByTrainingStartDesc(model)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ModelTrainingHistoryDTO create(ModelTrainingHistoryDTO dto, String username) {

        MLModel model = mlModelRepository.findById(dto.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo ML no encontrado: " + dto.getModelId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        ModelTrainingHistory h = new ModelTrainingHistory();
        h.setModel(model);
        h.setTrainingStart(dto.getTrainingStart() != null ? dto.getTrainingStart() : LocalDateTime.now());
        h.setTrainingEnd(dto.getTrainingEnd());
        h.setDataPointsUsed(dto.getDataPointsUsed());
        h.setR2Before(dto.getR2Before());
        h.setR2After(dto.getR2After());
        h.setStatus(dto.getStatus());
        h.setErrorMessage(dto.getErrorMessage());
        h.setCreatedBy(user);
        h.setCreatedAt(LocalDateTime.now());

        ModelTrainingHistory saved = historyRepository.save(h);
        return toDTO(saved);
    }

    private ModelTrainingHistoryDTO toDTO(ModelTrainingHistory h) {
        ModelTrainingHistoryDTO dto = new ModelTrainingHistoryDTO();
        dto.setId(h.getId());
        dto.setModelId(h.getModel().getId());
        dto.setModelName(h.getModel().getModelName());
        dto.setTrainingStart(h.getTrainingStart());
        dto.setTrainingEnd(h.getTrainingEnd());
        dto.setDataPointsUsed(h.getDataPointsUsed());
        dto.setR2Before(h.getR2Before());
        dto.setR2After(h.getR2After());
        dto.setStatus(h.getStatus());
        dto.setErrorMessage(h.getErrorMessage());
        dto.setCreatedAt(h.getCreatedAt());
        if (h.getCreatedBy() != null) {
            dto.setCreatedByUsername(h.getCreatedBy().getUsername());
        }
        return dto;
    }
}
