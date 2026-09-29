package com.handegunaydin.habit_tracker.jwt;

import com.handegunaydin.habit_tracker.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("bu-en-az-32-karakter-uzunlugunda-test-secret-key-olmali");

        ReflectionTestUtils.setField(jwtService, "expirationTime", 3600000L); // 1 saat (ms cinsinden)

    }

    @Test
    void shouldGenerateValidToken_whenUserDataProvided() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertNotNull(token);
    }

    @Test
    void shouldExtractCorrectUsername_fromGeneratedToken() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertEquals("test@test.com", jwtService.extractUserName(token));
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldReturnTrue_whenTokenIsValid() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertTrue(jwtService.isTokenValid(token, "test@test.com"));
    }

    @Test
    void shouldReturnFalse_whenUsernameDoesNotMatch() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertFalse(jwtService.isTokenValid(token, "test123@test123.com"));
    }

    @Test
    void shouldReturnFalse_whenTokenIsExpired() {
        UUID sessionID = UUID.randomUUID();
        ReflectionTestUtils.setField(jwtService, "expirationTime", -1000L);
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
    }

    @Test
    void generateToken_shouldEmbedRolesAsStringList() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertEquals(List.of("CUSTOMER"), jwtService.extractRoles(token));
    }

    @Test
    void extractRoles_shouldPreserveAllRoles_whenMultipleRolesExist() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER, Role.ADMIN), sessionID);
        assertEquals(List.of("CUSTOMER", "ADMIN"), jwtService.extractRoles(token));
    }

    @Test
    void shouldReturnEmptyList_whenRolesAreNull() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", null, sessionID);
        assertEquals(List.of(), jwtService.extractRoles(token));

    }

    @Test
    void extractJTI_shouldReturnUUID() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertNotNull(jwtService.extractID(token));
        assertDoesNotThrow(() -> UUID.fromString(jwtService.extractID(token)));
    }

    @Test
    void extractIssuedTime_shouldNotBeInFuture() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertNotNull(jwtService.extractIssuedTime(token));
        assertFalse(Instant.now().isBefore(jwtService.extractIssuedTime(token).toInstant()));
    }

    @Test
    void extractSessionId_shouldBeEqualToGivenId() {
        UUID sessionID = UUID.randomUUID();
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER), sessionID);
        assertNotNull(jwtService.extractSessionId(token));
        assertEquals(sessionID.toString(), jwtService.extractSessionId(token));
    }


}
