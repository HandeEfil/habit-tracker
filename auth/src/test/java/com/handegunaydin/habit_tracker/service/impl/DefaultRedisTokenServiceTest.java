package com.handegunaydin.habit_tracker.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultRedisTokenServiceTest {
    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private DefaultRedisTokenService defaultRedisTokenService;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(defaultRedisTokenService, "accessTokenDuration", 900000);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void disableAccessToken_shouldSetKeyWithPrefixAndTtl() {
        ArgumentCaptor<Duration> captor = ArgumentCaptor.forClass(Duration.class);
        defaultRedisTokenService.disableAccessToken("abc");
        verify(valueOperations).set(eq("blacklist:abc"), eq("disabled"), captor.capture());
        assertEquals(900, captor.getValue().getSeconds());
    }

    @Test
    void isAccessTokenDisabled_ifKeyExists_shouldReturnTrue() {
        when(valueOperations.get("blacklist:abc")).thenReturn("disabled");
        assertTrue(defaultRedisTokenService.isAccessTokenDisabled("abc"));
    }

    @Test
    void isAccessTokenDisabled_ifRedisConnectionFails_shouldReturnFalse() {
        when(valueOperations.get("blacklist:abc")).thenThrow(RedisConnectionFailureException.class);
        assertFalse(defaultRedisTokenService.isAccessTokenDisabled("abc"));
    }

    @Test
    void disableAllAccessTokensForUser_shouldSetKeyWithPrefixAndTtl() {
        ArgumentCaptor<Duration> captor = ArgumentCaptor.forClass(Duration.class);
        defaultRedisTokenService.disableAllAccessTokensForUser("test@test.com");
        verify(valueOperations).set(eq("blacklist:test@test.com"), any(), captor.capture());
        assertEquals(900, captor.getValue().getSeconds());

    }

    @Test
    void isAllAccessTokensForUserDisabled_ifIssuedAfterDisable_shouldReturnFalse() {
        Instant now = Instant.now();
        when(valueOperations.get("blacklist:test@test.com")).thenReturn(now.minusSeconds(600).toString());
        boolean allAccessTokensForUserDisabled = defaultRedisTokenService.isAllAccessTokensForUserDisabled("test@test.com", Date.from(now));
        assertFalse(allAccessTokensForUserDisabled);

    }

    @Test
    void isAllAccessTokensForUserDisabled_ifIssuedBeforeDisable_shouldReturnTrue() {
        Instant now = Instant.now();
        when(valueOperations.get("blacklist:test@test.com")).thenReturn(now.toString());
        boolean allAccessTokensForUserDisabled = defaultRedisTokenService.isAllAccessTokensForUserDisabled("test@test.com", Date.from(now.minusSeconds(600)));
        assertTrue(allAccessTokensForUserDisabled);
    }

    @Test
    void isAllAccessTokensForUserDisabled_ifNoRecord_shouldReturnFalse() {
        when(valueOperations.get("blacklist:test@test.com")).thenReturn(null);
        boolean allAccessTokensForUserDisabled = defaultRedisTokenService.isAllAccessTokensForUserDisabled("test@test.com", Date.from(Instant.now()));
        assertFalse(allAccessTokensForUserDisabled);
    }

}
