package com.handegunaydin.habit_tracker.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService( "bu-en-az-32-karakter-uzunlugunda-test-secret-key-olmali");

        ReflectionTestUtils.setField(jwtService, "expirationTime", 3600000L); // 1 saat (ms cinsinden)

    }

    @Test
    void shouldGenerateValidToken_whenUserDataProvided() {
        String token = jwtService.generateToken("test@test.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractCorrectUsername_fromGeneratedToken() {
        String token = jwtService.generateToken("test@test.com");
        assertEquals("test@test.com", jwtService.extractUserName(token));
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldReturnTrue_whenTokenIsValid() {
        String token = jwtService.generateToken("test@test.com");
        assertTrue(jwtService.isTokenValid(token, "test@test.com"));
    }

    @Test
    void shouldReturnFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken("test@test.com");
        assertFalse(jwtService.isTokenValid(token, "test123@test123.com"));
    }

    @Test
    void shouldReturnFalse_whenTokenIsExpired() {
        ReflectionTestUtils.setField(jwtService, "expirationTime", -1000L);
        String token = jwtService.generateToken("test@test.com");
        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.isTokenExpired(token);
        });
    }

}
