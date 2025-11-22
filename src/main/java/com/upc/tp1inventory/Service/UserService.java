package com.upc.tp1inventory.Service;

import com.upc.tp1inventory.DTO.UserCreateRequest;
import com.upc.tp1inventory.DTO.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDTO> getAllUsers();

    UserDTO getUserById(UUID id);

    UserDTO createUser(UserCreateRequest request);

    UserDTO updateUser(UUID id, UserDTO dto);

    void deleteUser(UUID id);

    UserDTO getCurrentUser(String username);
}
