package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.MLModelDTO;
import com.upc.tp1inventory.Entity.MLModel;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.MLModelRepository;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.MLModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MLModelServiceImpl implements MLModelService {

    private final MLModelRepository mlModelRepository;
    private final UserRepository userRepository;

    public MLModelServiceImpl(MLModelRepository mlModelRepository,
                              UserRepository userRepository) {
        this.mlModelRepository = mlModelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<MLModelDTO> getAll() {
        return mlModelRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MLModelDTO getById(UUID id) {
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo ML no encontrado: " + id));
        return toDTO(model);
    }

    @Override
    public MLModelDTO create(MLModelDTO dto, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        MLModel model = new MLModel();
        model.setModelName(dto.getModelName());
        model.setModelType(dto.getModelType());
        model.setVersion(dto.getVersion());
        model.setParameters(dto.getParameters());
        model.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
        model.setAccuracy(dto.getAccuracy());
        model.setMae(dto.getMae());
        model.setRmse(dto.getRmse());
        model.setTrainedAt(dto.getTrainedAt() != null ? dto.getTrainedAt() : LocalDateTime.now());
        model.setCreatedBy(user);
        model.setCreatedAt(LocalDateTime.now());

        MLModel saved = mlModelRepository.save(model);
        return toDTO(saved);
    }

    @Override
    public MLModelDTO update(UUID id, MLModelDTO dto) {
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo ML no encontrado: " + id));

        if (dto.getModelName() != null) model.setModelName(dto.getModelName());
        if (dto.getModelType() != null) model.setModelType(dto.getModelType());
        if (dto.getVersion() != null) model.setVersion(dto.getVersion());
        if (dto.getParameters() != null) model.setParameters(dto.getParameters());
        if (dto.getAccuracy() != null) model.setAccuracy(dto.getAccuracy());
        if (dto.getMae() != null) model.setMae(dto.getMae());
        if (dto.getRmse() != null) model.setRmse(dto.getRmse());
        if (dto.getTrainedAt() != null) model.setTrainedAt(dto.getTrainedAt());
        if (dto.getIsActive() != null) model.setIsActive(dto.getIsActive());

        MLModel saved = mlModelRepository.save(model);
        return toDTO(saved);
    }

    @Override
    public MLModelDTO setActive(UUID id, boolean active) {
        MLModel model = mlModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo ML no encontrado: " + id));

        model.setIsActive(active);
        MLModel saved = mlModelRepository.save(model);
        return toDTO(saved);
    }

    @Override
    public MLModelDTO getActiveByName(String modelName) {
        MLModel model = mlModelRepository
                .findFirstByModelNameAndIsActiveTrueOrderByCreatedAtDesc(modelName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay modelo activo para el nombre: " + modelName
                ));
        return toDTO(model);
    }

    private MLModelDTO toDTO(MLModel model) {
        MLModelDTO dto = new MLModelDTO();
        dto.setId(model.getId());
        dto.setModelName(model.getModelName());
        dto.setModelType(model.getModelType());
        dto.setVersion(model.getVersion());
        dto.setParameters(model.getParameters());
        dto.setIsActive(model.getIsActive());
        dto.setAccuracy(model.getAccuracy());
        dto.setMae(model.getMae());
        dto.setRmse(model.getRmse());
        dto.setTrainedAt(model.getTrainedAt());
        dto.setCreatedAt(model.getCreatedAt());
        if (model.getCreatedBy() != null) {
            dto.setCreatedByUsername(model.getCreatedBy().getUsername());
        }
        return dto;
    }
}
