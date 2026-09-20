package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class)
public class DefaultTokenGeneratorTest {

    @InjectMocks
    private DefaultTokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                tokenGenerator, "expirationDurationForRefreshToken", 900000L);
    }

    @Test
    void generateRawToken_shouldReturnNonNullNonEmptyToken() {
        assertTrue(StringUtils.isNotBlank(tokenGenerator.generateRawToken()));
    }

    @Test
    void generateRawToken_shouldReturnDifferentValuesOnEachCall() {
        Assertions.assertNotEquals(tokenGenerator.generateRawToken(), tokenGenerator.generateRawToken());
    }

    @Test
    void generateRawToken_shouldBeValidBase64Url() {
        Assertions.assertDoesNotThrow(() -> {
            Base64.getUrlDecoder().decode(tokenGenerator.generateRawToken());
        });
    }

    @Test
    void getTokenHash_shouldReturnSameHashForSameInput() {
        assertEquals(tokenGenerator.getTokenHash("abc"), tokenGenerator.getTokenHash("abc"));
    }

    @Test
    void getTokenHash_shouldReturnDifferentHashForDifferentInput() {
        assertNotEquals(tokenGenerator.getTokenHash("abc"), tokenGenerator.getTokenHash("abd"));
    }

    @Test
    void getTokenHash_shouldReturn64CharacterHexString() {
        String hash = tokenGenerator.getTokenHash("test-token");

        assertEquals(64, hash.length());

        assertTrue(hash.chars()
                .allMatch(c ->
                        Character.digit(c, 16) != -1
                ));
    }

    @Test
    void saveHashedToken_shouldSetCorrectMailAndTokenHash() {
        String tokenHash = tokenGenerator.getTokenHash("abc");
        RefreshToken refreshToken = tokenGenerator.populateHashedToken("test@test.com", tokenHash);

        assertEquals("test@test.com", refreshToken.getEmail());
        assertEquals(tokenHash, refreshToken.getTokenHashed());


    }

    @Test
    void saveHashedToken_shouldSetExpiresAtCorrectly() {
        String tokenHash = tokenGenerator.getTokenHash("abc");
        Instant before = Instant.now().plusMillis(900000L);
        RefreshToken refreshToken = tokenGenerator.populateHashedToken("test@test.com", tokenHash);
        Instant after = Instant.now().plusMillis(900000L);
        assertTrue(before.isBefore(refreshToken.getExpiresAt()) || before.equals(refreshToken.getExpiresAt()));
        assertTrue(after.isAfter(refreshToken.getExpiresAt()) || before.equals(refreshToken.getExpiresAt()));

    }


}