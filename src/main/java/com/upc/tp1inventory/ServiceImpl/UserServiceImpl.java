package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.UserCreateRequest;
import com.upc.tp1inventory.DTO.UserDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
        return toDTO(user);
    }

    @Override
    public UserDTO createUser(UserCreateRequest request) {
        validateCreateRequest(request);

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();
        String roleStr = request.getRole().trim().toUpperCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya se encuentra registrado.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
        }

        User.Role role;
        try {
            role = User.Role.valueOf(roleStr);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("El rol seleccionado no es válido.");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone == null || phone.isBlank() ? null : phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        user.setRole(role);
        user.setIsActive(true);
        user.setMustChangePassword(true);

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    @Override
    public UserDTO updateUser(UUID id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));

        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }

        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim().toLowerCase();

        userRepository.findByUsernameIgnoreCase(username).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("El nombre de usuario ya se encuentra registrado.");
            }
        });

        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("El correo electrónico ya se encuentra registrado.");
            }
        });

        user.setFullName(dto.getFullName().trim());
        user.setEmail(email);
        user.setUsername(username);

        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            try {
                user.setRole(User.Role.valueOf(dto.getRole().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("El rol seleccionado no es válido.");
            }
        }

        if (dto.getIsActive() != null) {
            user.setIsActive(dto.getIsActive());
        }

        User saved = userRepository.save(user);
        return toDTO(saved);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        return toDTO(user);
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de creación de usuario es obligatoria.");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña temporal es obligatoria.");
        }

        if (request.getPassword().trim().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole().name());
        dto.setIsActive(user.getIsActive());
        dto.setMustChangePassword(user.getMustChangePassword());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
