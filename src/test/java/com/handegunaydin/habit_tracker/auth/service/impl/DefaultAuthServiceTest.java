package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.dto.TokenPairResponseDTO;
import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import com.handegunaydin.habit_tracker.auth.enums.Role;
import com.handegunaydin.habit_tracker.auth.jwt.JwtService;
import com.handegunaydin.habit_tracker.auth.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.auth.service.TokenChainRevocationService;
import com.handegunaydin.habit_tracker.auth.service.TokenGenerator;
import io.micrometer.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultAuthServiceTest {

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenChainRevocationService detectRefreshTokenChain;

    @InjectMocks
    private DefaultAuthService authService;


    @Test
    void generateRefreshToken_shouldCallTokenGeneratorMethodsInCorrectOrder() {
        authService.generateRefreshToken("test@test.com");
        InOrder inOrder = Mockito.inOrder(tokenGenerator);
        inOrder.verify(tokenGenerator).generateRawToken();
        inOrder.verify(tokenGenerator).getTokenHash(any());
        inOrder.verify(tokenGenerator).populateHashedToken(any(), any());
    }

    @Test
    void generateRefreshToken_shouldPassRawTokenHashToSaveHashedToken() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(tokenGenerator.generateRawToken()).thenReturn("abc");
        when(tokenGenerator.getTokenHash("abc")).thenReturn("abc-hash");
        authService.generateRefreshToken("test@test.com");
        verify(tokenGenerator).populateHashedToken(eq("test@test.com"), captor.capture());
        assertEquals("abc-hash", captor.getValue());
    }

    @Test
    void refresh_whenTokenValidAndNotRevoked_shouldReturnNewTokenPair() {
        RefreshToken oldToken = tokenCreator("old-hash");
        RefreshToken newToken = tokenCreator("new-hash");
        TokenPairResponseDTO tokenDTO = triggerRefresh(oldToken, newToken);
        assertTrue(StringUtils.isNotBlank(tokenDTO.accessToken()));
        assertTrue(StringUtils.isNotBlank(tokenDTO.refreshToken()));

    }

    @Test
    void refresh_whenTokenValid_shouldMarkOldTokenAsRevoked() {
        RefreshToken oldToken = tokenCreator("old-hash");
        RefreshToken newToken = tokenCreator("new-hash");
        triggerRefresh(oldToken, newToken);
        assertTrue(oldToken.isRevoked());

    }

    @Test
    void refresh_whenTokenValid_shouldGenerateNewAccessTokenForCorrectEmail() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        RefreshToken oldToken = tokenCreator("old-hash");
        RefreshToken newToken = tokenCreator("new-hash");
        triggerRefresh(oldToken, newToken);
        verify(jwtService).generateToken(captor.capture(), List.of(Role.CUSTOMER));
        assertEquals(captor.getValue(), newToken.getEmail());
    }

    @Test
    void refresh_whenTokenValid_shouldReturnRawTokenNotHashInResponse() {
        RefreshToken oldToken = tokenCreator("old-hash");
        RefreshToken newToken = tokenCreator("new-hash");
        TokenPairResponseDTO tokenDTO = triggerRefresh(oldToken, newToken);
        assertEquals("new", tokenDTO.refreshToken());
    }

    @Test
    void refresh_whenTokenValid_shouldNotTriggerChainDetection() {
        RefreshToken oldToken = tokenCreator("old-hash");
        RefreshToken newToken = tokenCreator("new-hash");
        triggerRefresh(oldToken, newToken);
        verify(detectRefreshTokenChain, never()).detectRefreshTokenChain(any());

    }

    @Test
    void refresh_whenTokenNotFound_shouldThrowBadCredentialsException() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));
    }

    @Test
    void refresh_whenTokenNotFound_shouldNotCallRevokeIfActive() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));
        verify(refreshTokenRepository, never()).revokeIfActive(any());
    }

    @Test
    void refresh_whenTokenAlreadyRevoked_shouldThrowBadCredentialsException() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.revokeIfActive(any())).thenReturn(0);
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(tokenCreator("old-hash")));
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));
    }

    @Test
    void refresh_whenTokenAlreadyRevoked_shouldNotGenerateNewToken() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.revokeIfActive(any())).thenReturn(0);
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(tokenCreator("old-hash")));
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));
        verify(tokenGenerator, never()).generateRawToken();

    }

    @Test
    void refresh_whenReusedTokenIsChainEnd_shouldRevokeItDirectly() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.revokeIfActive(any())).thenReturn(0);
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(tokenCreator("old-hash")));
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));
        verify(detectRefreshTokenChain, times(1)).detectRefreshTokenChain(any());

    }

    @Test
    void refresh_whenTokenExpired_shouldThrowBadCredentialsException() {

        RefreshToken refreshToken = tokenCreator("old-hash");
        refreshToken.setExpiresAt(Instant.now().minusMillis(900000L));
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(refreshToken));
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));


    }

    @Test
    void refresh_whenTokenExpired_shouldNotGenerateNewToken() {
        RefreshToken refreshToken = tokenCreator("old-hash");
        refreshToken.setExpiresAt(Instant.now().minusMillis(900000L));
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(refreshToken));
        assertThrows(BadCredentialsException.class, () -> authService.refresh("old"));
        verify(tokenGenerator, never()).generateRawToken();
    }

    private TokenPairResponseDTO triggerRefresh(RefreshToken oldToken, RefreshToken newToken) {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(tokenGenerator.generateRawToken()).thenReturn("new");
        when(tokenGenerator.getTokenHash("new")).thenReturn("new-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(oldToken));
        when(tokenGenerator.populateHashedToken("test@test.com", "new-hash")).thenReturn(newToken);
        when(refreshTokenRepository.revokeIfActive("old-hash")).thenReturn(1);
        when(jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER))).thenReturn("jwt-token");
        when(refreshTokenRepository.save(any())).thenReturn(newToken);
        return authService.refresh("old");
    }


    @Test
    void revokeAllForUser_shouldCallRepositoryWithCorrectMail() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        authService.revokeAllForUser("random@mail.com");
        verify(refreshTokenRepository).revokeAllForUser(captor.capture());
        assertEquals("random@mail.com", captor.getValue());
    }

    private static @NotNull RefreshToken tokenCreator(String hashToken) {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setTokenHashed(hashToken);
        oldToken.setEmail("test@test.com");
        oldToken.setExpiresAt(Instant.now().plusMillis(900000L));
        return oldToken;
    }
}
