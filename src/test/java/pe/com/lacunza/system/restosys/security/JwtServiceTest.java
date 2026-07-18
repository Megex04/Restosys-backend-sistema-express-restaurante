package pe.com.lacunza.system.restosys.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String USERNAME = "usuario@lacunza.com";
    private static final String OTRO_USERNAME = "otro@lacunza.com";
    private static final long ONE_HOUR_MS = 1000 * 60 * 60;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetails otroUsuario;

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // secretKey y jwtExpiration se inyectan con @Value en producción (vía Spring).
        // Aquí no hay contenedor de Spring, así que se setean a mano por reflection.
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        setField(jwtService, "secretKey", Base64.getEncoder().encodeToString(keyBytes));
        setField(jwtService, "jwtExpiration", ONE_HOUR_MS);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void generateToken_deberiaCrearTokenValidoConElUsername() {
        when(userDetails.getUsername()).thenReturn(USERNAME);

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(USERNAME, jwtService.extractUsername(token));
    }

    @Test
    void generateToken_conExtraClaims_deberiaIncluirlosEnElToken() {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");

        String token = jwtService.generateToken(extraClaims, userDetails);

        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertEquals("ADMIN", role);
        assertEquals(USERNAME, jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_deberiaRetornarElSubjectDelToken() {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        assertEquals(USERNAME, jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_conTokenMalformado_deberiaLanzarExcepcion() {
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername("token-invalido"));
    }

    @Test
    void extractClaim_deberiaExtraerLaExpirationDespuesDelIssuedAt() {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        assertTrue(expiration.after(issuedAt));
    }

    // --- isTokenValid: camino "feliz" (no entra al guard clause) ---

    @Test
    void isTokenValid_conTokenYUsuarioCorrecto_deberiaSerTrue() {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_conUsuarioDistinto_deberiaSerFalse() {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);
        when(otroUsuario.getUsername()).thenReturn(OTRO_USERNAME);

        assertFalse(jwtService.isTokenValid(token, otroUsuario));
    }

    @Test
    void isTokenValid_conTokenExpirado_deberiaSerFalse() throws Exception {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        setField(jwtService, "jwtExpiration", -ONE_HOUR_MS);
        String tokenExpirado = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(tokenExpirado, userDetails));
    }

    // --- isTokenValid: guard clause "if (token == null || token.isBlank() || userDetails == null)" ---
    // Por el short-circuit del OR, cada condición solo se evalúa si las anteriores fueron false,
    // así que se cubre cada rama por separado en vez de probar las 8 combinaciones booleanas.

    @Test
    void isTokenValid_conTokenNulo_deberiaSerFalse() {
        assertFalse(jwtService.isTokenValid(null, userDetails));
    }

    @Test
    void isTokenValid_conTokenVacio_deberiaSerFalse() {
        assertFalse(jwtService.isTokenValid("", userDetails));
    }

    @Test
    void isTokenValid_conTokenSoloEspacios_deberiaSerFalse() {
        assertFalse(jwtService.isTokenValid("   ", userDetails));
    }

    @Test
    void isTokenValid_conUserDetailsNulo_deberiaSerFalse() {
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        assertFalse(jwtService.isTokenValid(token, null));
    }

    @Test
    void isTokenValid_conTokenYUserDetailsAmbosNulos_deberiaSerFalse() {
        assertFalse(jwtService.isTokenValid(null, null));
    }
}
