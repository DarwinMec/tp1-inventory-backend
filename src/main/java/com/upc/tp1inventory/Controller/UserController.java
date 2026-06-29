package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.UserCreateRequest;
import com.upc.tp1inventory.DTO.UserDTO;
import com.upc.tp1inventory.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO getById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateRequest request) {
        UserDTO created = userService.createUser(request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO update(@PathVariable UUID id, @RequestBody UserDTO dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
