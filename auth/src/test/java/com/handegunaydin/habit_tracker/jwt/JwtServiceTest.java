package com.handegunaydin.habit_tracker.jwt;

import com.handegunaydin.habit_tracker.enums.Role;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

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
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractCorrectUsername_fromGeneratedToken() {
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        assertEquals("test@test.com", jwtService.extractUserName(token));
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldReturnTrue_whenTokenIsValid() {
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        assertTrue(jwtService.isTokenValid(token, "test@test.com"));
    }

    @Test
    void shouldReturnFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        assertFalse(jwtService.isTokenValid(token, "test123@test123.com"));
    }

    @Test
    void shouldReturnFalse_whenTokenIsExpired() {
        ReflectionTestUtils.setField(jwtService, "expirationTime", -1000L);
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
    }

    @Test
    void generateToken_shouldEmbedRolesAsStringList() {
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        assertEquals(List.of("CUSTOMER"), jwtService.extractRoles(token));
    }

    @Test
    void extractRoles_shouldPreserveAllRoles_whenMultipleRolesExist() {
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER, Role.ADMIN));
        assertEquals(List.of("CUSTOMER", "ADMIN"), jwtService.extractRoles(token));
    }

    @Test
    void extractRoles_shouldHandleGracefully_whenRolesClaimMissing() {
        String token = jwtService.generateToken("test@test.com", null);
        assertEquals(List.of(), jwtService.extractRoles(token));

    }

}
