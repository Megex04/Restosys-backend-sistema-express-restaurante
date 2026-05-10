package pe.com.lacunza.system.restosys.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pe.com.lacunza.system.restosys.dtos.AuthRequestDTO;
import pe.com.lacunza.system.restosys.dtos.AuthResponseDTO;
import pe.com.lacunza.system.restosys.dtos.PinAuthRequestDTO;
import pe.com.lacunza.system.restosys.dtos.RegisterRequest;
import pe.com.lacunza.system.restosys.entity.User;
import pe.com.lacunza.system.restosys.repository.UserRepository;
import pe.com.lacunza.system.restosys.security.JwtService;
import pe.com.lacunza.system.restosys.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // 1. Login tradicional (Usuario y Contraseña)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(token, user.getUsername(), user.getRole().name()));
    }

    // 2. Login rápido para POS (Solo PIN)
    @PostMapping("/login/pin")
    public ResponseEntity<?> loginWithPin(@RequestBody PinAuthRequestDTO request) {
        User user = userRepository.findByPin(request.getPin())
                .orElseThrow(() -> new RuntimeException("PIN incorrecto"));

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(token, user.getUsername(), user.getRole().name()));
    }
}
