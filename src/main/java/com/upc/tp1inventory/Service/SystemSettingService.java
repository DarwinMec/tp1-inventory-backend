package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.SystemSettingDTO;

import java.util.List;

public interface SystemSettingService {

    List<SystemSettingDTO> getAll();

    SystemSettingDTO getByKey(String key);

    SystemSettingDTO create(SystemSettingDTO dto, String username);

    SystemSettingDTO update(String key, SystemSettingDTO dto, String username);

    void delete(String key);
}
