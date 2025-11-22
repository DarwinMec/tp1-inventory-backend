package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.SystemSettingDTO;
import com.upc.tp1inventory.Entity.SystemSetting;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.SystemSettingRepository;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.SystemSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final UserRepository userRepository;

    public SystemSettingServiceImpl(SystemSettingRepository systemSettingRepository,
                                    UserRepository userRepository) {
        this.systemSettingRepository = systemSettingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<SystemSettingDTO> getAll() {
        return systemSettingRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemSettingDTO getByKey(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada: " + key));
        return toDTO(setting);
    }

    @Override
    public SystemSettingDTO create(SystemSettingDTO dto, String username) {
        if (dto.getSettingKey() == null || dto.getSettingKey().isBlank()) {
            throw new IllegalArgumentException("settingKey es obligatorio");
        }

        systemSettingRepository.findBySettingKey(dto.getSettingKey())
                .ifPresent(s -> {
                    throw new IllegalArgumentException("Ya existe una configuración con la clave: " + dto.getSettingKey());
                });

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(dto.getSettingKey());
        setting.setSettingValue(dto.getSettingValue());
        setting.setDataType(dto.getDataType());
        setting.setDescription(dto.getDescription());
        setting.setUpdatedBy(user);

        SystemSetting saved = systemSettingRepository.save(setting);
        return toDTO(saved);
    }

    @Override
    public SystemSettingDTO update(String key, SystemSettingDTO dto, String username) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada: " + key));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        if (dto.getSettingValue() != null) {
            setting.setSettingValue(dto.getSettingValue());
        }
        if (dto.getDataType() != null) {
            setting.setDataType(dto.getDataType());
        }
        if (dto.getDescription() != null) {
            setting.setDescription(dto.getDescription());
        }

        setting.setUpdatedBy(user);

        SystemSetting saved = systemSettingRepository.save(setting);
        return toDTO(saved);
    }

    @Override
    public void delete(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada: " + key));
        systemSettingRepository.delete(setting);
    }

    // ====== helpers ======
    private SystemSettingDTO toDTO(SystemSetting entity) {
        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setId(entity.getId());
        dto.setSettingKey(entity.getSettingKey());
        dto.setSettingValue(entity.getSettingValue());
        dto.setDataType(entity.getDataType());
        dto.setDescription(entity.getDescription());
        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedByUsername(entity.getUpdatedBy().getUsername());
        }
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
