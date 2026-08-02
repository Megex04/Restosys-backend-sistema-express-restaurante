package pe.com.lacunza.system.restosys.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.com.lacunza.system.restosys.dtos.AuthResponseDTO;
import pe.com.lacunza.system.restosys.dtos.RegisterRequest;
import pe.com.lacunza.system.restosys.dtos.Role;
import pe.com.lacunza.system.restosys.entity.User;
import pe.com.lacunza.system.restosys.repository.UserRepository;
import pe.com.lacunza.system.restosys.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USERNAME = "admin";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded-password-hash";
    private static final String PIN = "1234";
    private static final String TOKEN = "fake-jwt-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private RegisterRequest buildRequest(String role) {
        return RegisterRequest.builder()
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .role(role)
                .pin(PIN)
                .build();
    }

    @Test
    void register_conRolValido_deberiaGuardarUsuarioEncriptadoYRetornarToken() {
        // Arrange
        RegisterRequest request = buildRequest("ROLE_ADMIN");
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(any(User.class))).thenReturn(TOKEN);

        // Act
        AuthResponseDTO response = authServiceImpl.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(TOKEN, response.getToken());
        assertEquals(USERNAME, response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User usuarioGuardado = userCaptor.getValue();
        assertEquals(USERNAME, usuarioGuardado.getUsername());
        assertEquals(ENCODED_PASSWORD, usuarioGuardado.getPassword());
        assertEquals(Role.ROLE_ADMIN, usuarioGuardado.getRole());
        assertEquals(PIN, usuarioGuardado.getPin());

        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(jwtService).generateToken(usuarioGuardado);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void register_conCadaRolValido_deberiaAsignarloCorrectamente(Role role) {
        // Arrange
        RegisterRequest request = buildRequest(role.name());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(any(User.class))).thenReturn(TOKEN);

        // Act
        AuthResponseDTO response = authServiceImpl.register(request);

        // Assert
        assertEquals(role.name(), response.getRole());
    }

    @Test
    void register_noDeberiaGuardarLaContrasenaEnTextoPlano() {
        // Arrange
        RegisterRequest request = buildRequest("ROLE_WAITER");
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(any(User.class))).thenReturn(TOKEN);

        // Act
        authServiceImpl.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotEquals(RAW_PASSWORD, userCaptor.getValue().getPassword());
    }

    @Test
    void register_conRolInvalido_deberiaLanzarIllegalArgumentExceptionYNoGuardarNada() {
        // Arrange
        RegisterRequest request = buildRequest("ROL_QUE_NO_EXISTE");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> authServiceImpl.register(request));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_conRolNulo_deberiaLanzarNullPointerExceptionYNoGuardarNada() {
        // Arrange
        RegisterRequest request = buildRequest(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> authServiceImpl.register(request));

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_noDeberiaInteractuarConAuthenticationManager() {
        // Arrange
        RegisterRequest request = buildRequest("ROLE_CASHIER");
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(jwtService.generateToken(any(User.class))).thenReturn(TOKEN);

        // Act
        authServiceImpl.register(request);

        // Assert
        verifyNoInteractions(authenticationManager);
    }
}
