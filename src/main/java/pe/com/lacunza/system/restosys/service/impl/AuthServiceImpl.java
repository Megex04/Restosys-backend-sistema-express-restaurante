package pe.com.lacunza.system.restosys.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.com.lacunza.system.restosys.dtos.AuthResponseDTO;
import pe.com.lacunza.system.restosys.dtos.RegisterRequest;
import pe.com.lacunza.system.restosys.dtos.Role;
import pe.com.lacunza.system.restosys.entity.User;
import pe.com.lacunza.system.restosys.repository.UserRepository;
import pe.com.lacunza.system.restosys.security.JwtService;
import pe.com.lacunza.system.restosys.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDTO register(RegisterRequest request) {
        // 1. Creamos el usuario con los datos del request
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // ¡Encriptamos la contraseña!
                .role(Role.valueOf(request.getRole())) // Asignamos el rol
                .pin(request.getPin()) // Asignamos el PIN
                .build();

        // 2. Lo guardamos en Postgres
        userRepository.save(user);

        // 3. Generamos el token para que ya quede logueado al registrarse
        var jwtToken = jwtService.generateToken(user);

        // 4. Devolvemos el token
        return AuthResponseDTO.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
