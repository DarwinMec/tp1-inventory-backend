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
            var authToken = new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            );
            authenticationManager.authenticate(authToken);

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    // Nuevo endpoint: perfil del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal User user) {
        // user ya viene del SecurityContext gracias al filtro JWT
        UserDTO dto = userService.getCurrentUser(user.getUsername());
        return ResponseEntity.ok(dto);
    }
}
