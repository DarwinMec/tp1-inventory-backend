package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.AuthRequest;
import com.upc.tp1inventory.DTO.AuthResponse;
import com.upc.tp1inventory.DTO.ChangePasswordRequest;
import com.upc.tp1inventory.DTO.UserDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Security.JwtService;
import com.upc.tp1inventory.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String identifier = request.getUsername();

            if (identifier == null || identifier.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Debe ingresar usuario o email"));
            }

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Debe ingresar contraseña"));
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    identifier.trim(),
                    request.getPassword()
            );

            authenticationManager.authenticate(authToken);

            User user = userRepository.findByUsernameOrEmail(identifier.trim())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(new AuthResponse(token, user.getMustChangePassword()));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciales inválidas"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal User user) {
        UserDTO dto = userService.getCurrentUser(user.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User user = userRepository.findByUsernameIgnoreCase(authenticatedUser.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la contraseña temporal");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contraseña actualizada correctamente"
        ));
    }
}
