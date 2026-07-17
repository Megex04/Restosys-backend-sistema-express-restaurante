package pe.com.lacunza.system.restosys.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.com.lacunza.system.restosys.dtos.*;
import pe.com.lacunza.system.restosys.entity.User;
import pe.com.lacunza.system.restosys.repository.UserRepository;
import pe.com.lacunza.system.restosys.security.JwtService;
import pe.com.lacunza.system.restosys.service.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void register_shouldReturnOkWithAuthResponse() {
        // Arrange
        RegisterRequest request = mock(RegisterRequest.class);

        AuthResponseDTO expectedResponse = AuthResponseDTO.builder()
                .token("fake-jwt-token")
                .username("admin")
                .role("ADMIN")
                .build();

        when(authService.register(request)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponseDTO> response = authController.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("fake-jwt-token", response.getBody().getToken());
        assertEquals("admin", response.getBody().getUsername());
        assertEquals("ADMIN", response.getBody().getRole());

        verify(authService, times(1)).register(request);
        verifyNoMoreInteractions(authService);
    }

    @Test
    void login_success_200_ok() {
        // Arrange
        AuthRequestDTO request = mock(AuthRequestDTO.class);
        when(request.getUsername()).thenReturn("admin");
        when(request.getPassword()).thenReturn("password");

        User userFounded = User.builder()
                .username("admin")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.ofNullable(userFounded));
        when(passwordEncoder.matches(request.getPassword(), userFounded.getPassword())).thenReturn(true);
        when(jwtService.generateToken(userFounded)).thenReturn("fake-jwt-token");

        // Act
        ResponseEntity<?> responseLogin = authController.login(request);

        //Assert's
        assertNotNull(responseLogin);
        assertEquals(HttpStatus.OK, responseLogin.getStatusCode());
        assertEquals("fake-jwt-token", ((AuthResponseDTO) responseLogin.getBody()).getToken());
    }

    @Test
    void login_error_401_unauthorized() {

        // Arrange
        AuthRequestDTO request = mock(AuthRequestDTO.class);
        when(request.getUsername()).thenReturn("admin");
        when(request.getPassword()).thenReturn("wrongpassword");

        User userFounded = User.builder()
                .username("admin")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.ofNullable(userFounded));
        when(passwordEncoder.matches(request.getPassword(), userFounded.getPassword())).thenReturn(false);

        // Act
        ResponseEntity<?> responseLogin = authController.login(request);

        // Assert
        assertNotNull(responseLogin);
        assertEquals(HttpStatus.UNAUTHORIZED, responseLogin.getStatusCode());
        assertEquals(401, responseLogin.getStatusCode().value());
        assertEquals("Credenciales inválidas", responseLogin.getBody());
    }

    @Test
    void login_exception() {
        // Arrange
        AuthRequestDTO request = mock(AuthRequestDTO.class);
        when(request.getUsername()).thenReturn("user");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });

        // Assert
        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginWithPin_success() {
        // Arrange
        PinAuthRequestDTO request = mock(PinAuthRequestDTO.class);
        when(request.getPin()).thenReturn("1234");

        User userFounded = User.builder()
                .username("admin")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.findByPin(request.getPin())).thenReturn(Optional.of(userFounded));
        when(jwtService.generateToken(userFounded)).thenReturn("fake-jwt-token");

        // Act
        ResponseEntity<?> responseLogin = authController.loginWithPin(request);

        // Assert
        assertNotNull(responseLogin);
        assertEquals(HttpStatus.OK, responseLogin.getStatusCode());
        assertEquals("fake-jwt-token", ((AuthResponseDTO) responseLogin.getBody()).getToken());

    }
    @Test
    void loginWithPin_exception() {
        // Arrange
        PinAuthRequestDTO request = mock(PinAuthRequestDTO.class);
        when(request.getPin()).thenReturn("9999");

        when(userRepository.findByPin(request.getPin())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.loginWithPin(request);
        });

        // Assert
        assertEquals("PIN incorrecto", exception.getMessage());
        verify(jwtService, never()).generateToken(any());

    }
}