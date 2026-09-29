package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.RefreshTokenRequestDTO;
import com.handegunaydin.habit_tracker.dto.TokenPairResponseDTO;
import com.handegunaydin.habit_tracker.entity.RefreshToken;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.enums.Role;
import com.handegunaydin.habit_tracker.jwt.JwtService;
import com.handegunaydin.habit_tracker.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.TokenChainRevocationService;
import com.handegunaydin.habit_tracker.service.TokenGenerator;
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
import java.util.UUID;

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
    private UserRepository userRepository;

    @Mock
    private TokenChainRevocationService detectRefreshTokenChain;

    @InjectMocks
    private DefaultAuthService authService;


    @Test
    void generateRefreshToken_shouldCallTokenGeneratorMethodsInCorrectOrder() {
        UUID sessionID = UUID.randomUUID();
        authService.generateRefreshToken("test@test.com", sessionID);
        InOrder inOrder = Mockito.inOrder(tokenGenerator);
        inOrder.verify(tokenGenerator).generateRawToken();
        inOrder.verify(tokenGenerator).getTokenHash(any());
        inOrder.verify(tokenGenerator).populateHashedToken(any(), any(), eq(sessionID));
    }

    @Test
    void generateRefreshToken_shouldPassRawTokenHashToSaveHashedToken() {
        UUID sessionID = UUID.randomUUID();
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        when(tokenGenerator.generateRawToken()).thenReturn("abc");
        when(tokenGenerator.getTokenHash("abc")).thenReturn("abc-hash");
        authService.generateRefreshToken("test@test.com", sessionID);
        verify(tokenGenerator).populateHashedToken(eq("test@test.com"), captor.capture(), eq(sessionID));
        assertEquals("abc-hash", captor.getValue());
    }

    @Test
    void refresh_whenTokenValidAndNotRevoked_shouldReturnNewTokenPair() {
        RefreshToken oldToken = tokenCreator("old-hash", null);
        RefreshToken newToken = tokenCreator("new-hash", oldToken.getSessionId());
        TokenPairResponseDTO tokenDTO = triggerRefresh(oldToken, newToken);
        assertTrue(StringUtils.isNotBlank(tokenDTO.accessToken()));
        assertTrue(StringUtils.isNotBlank(tokenDTO.refreshToken()));

    }

    @Test
    void refresh_whenTokenValid_shouldMarkOldTokenAsRevoked() {
        RefreshToken oldToken = tokenCreator("old-hash", null);
        RefreshToken newToken = tokenCreator("new-hash", oldToken.getSessionId());
        triggerRefresh(oldToken, newToken);
        assertTrue(oldToken.isRevoked());

    }

    @Test
    void refresh_whenTokenValid_shouldGenerateNewAccessTokenForCorrectEmail() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        RefreshToken oldToken = tokenCreator("old-hash", null);
        RefreshToken newToken = tokenCreator("new-hash", oldToken.getSessionId());
        triggerRefresh(oldToken, newToken);
        verify(jwtService).generateToken(captor.capture(), any(), any());
        assertEquals(captor.getValue(), newToken.getEmail());
    }

    @Test
    void refresh_whenTokenValid_shouldReturnRawTokenNotHashInResponse() {
        RefreshToken oldToken = tokenCreator("old-hash", null);
        RefreshToken newToken = tokenCreator("new-hash", oldToken.getSessionId());
        TokenPairResponseDTO tokenDTO = triggerRefresh(oldToken, newToken);
        assertEquals("new", tokenDTO.refreshToken());
    }

    @Test
    void refresh_whenTokenValid_shouldNotTriggerChainDetection() {
        RefreshToken oldToken = tokenCreator("old-hash", null);
        RefreshToken newToken = tokenCreator("new-hash", oldToken.getSessionId());
        triggerRefresh(oldToken, newToken);
        verify(detectRefreshTokenChain, never()).detectRefreshTokenChain(any());

    }

    @Test
    void refresh_whenTokenNotFound_shouldThrowBadCredentialsException() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));
    }

    @Test
    void refresh_whenTokenNotFound_shouldNotCallRevokeIfActive() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));
        verify(refreshTokenRepository, never()).revokeIfActive(any());
    }

    @Test
    void refresh_whenTokenAlreadyRevoked_shouldThrowBadCredentialsException() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.revokeIfActive(any())).thenReturn(0);
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(tokenCreator("old-hash", null)));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));
    }

    @Test
    void refresh_whenTokenAlreadyRevoked_shouldNotGenerateNewToken() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.revokeIfActive(any())).thenReturn(0);
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(tokenCreator("old-hash", null)));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));
        verify(tokenGenerator, never()).generateRawToken();

    }

    @Test
    void refresh_whenReusedTokenIsChainEnd_shouldRevokeItDirectly() {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.revokeIfActive(any())).thenReturn(0);
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(tokenCreator("old-hash", null)));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));
        verify(detectRefreshTokenChain, times(1)).detectRefreshTokenChain(any());

    }

    @Test
    void refresh_whenTokenExpired_shouldThrowBadCredentialsException() {

        RefreshToken refreshToken = tokenCreator("old-hash", null);
        refreshToken.setExpiresAt(Instant.now().minusMillis(900000L));
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(refreshToken));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));


    }

    @Test
    void refresh_whenTokenExpired_shouldNotGenerateNewToken() {
        RefreshToken refreshToken = tokenCreator("old-hash", null);
        refreshToken.setExpiresAt(Instant.now().minusMillis(900000L));
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(refreshToken));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(new RefreshTokenRequestDTO("old")));
        verify(tokenGenerator, never()).generateRawToken();
    }

    private TokenPairResponseDTO triggerRefresh(RefreshToken oldToken, RefreshToken newToken) {
        when(tokenGenerator.getTokenHash("old")).thenReturn("old-hash");
        when(tokenGenerator.generateRawToken()).thenReturn("new");
        when(tokenGenerator.getTokenHash("new")).thenReturn("new-hash");
        when(refreshTokenRepository.findRefreshTokenByTokenHashed("old-hash")).thenReturn(Optional.of(oldToken));
        when(tokenGenerator.populateHashedToken("test@test.com", "new-hash", oldToken.getSessionId())).thenReturn(newToken);
        when(refreshTokenRepository.revokeIfActive("old-hash")).thenReturn(1);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("jwt-token");
        when(refreshTokenRepository.save(any())).thenReturn(newToken);
        User user = new User();
        user.setRoles(List.of(Role.CUSTOMER));
        when(userRepository.findByMail(any())).thenReturn(Optional.of(user));
        return authService.refresh(new RefreshTokenRequestDTO("old"));
    }


    @Test
    void revokeAllForUser_shouldCallRepositoryWithCorrectMail() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        authService.revokeAllForUser("random@mail.com");
        verify(refreshTokenRepository).revokeAllForUser(captor.capture());
        assertEquals("random@mail.com", captor.getValue());
    }

    private static @NotNull RefreshToken tokenCreator(String hashToken, UUID sessionID) {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setTokenHashed(hashToken);
        oldToken.setSessionId(sessionID != null ? sessionID : UUID.randomUUID());
        oldToken.setEmail("test@test.com");
        oldToken.setExpiresAt(Instant.now().plusMillis(900000L));
        return oldToken;
    }
}
