package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.AuthRequest;
import com.upc.tp1inventory.DTO.AuthResponse;
import com.upc.tp1inventory.DTO.UserDTO;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Security.JwtService;
import com.upc.tp1inventory.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String identifier = request.getUsername();

            if (identifier == null || identifier.isBlank()) {
                return ResponseEntity.badRequest().body("Debe ingresar usuario o email");
            }

            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body("Debe ingresar contraseña");
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

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal User user) {
        UserDTO dto = userService.getCurrentUser(user.getUsername());
        return ResponseEntity.ok(dto);
    }
}