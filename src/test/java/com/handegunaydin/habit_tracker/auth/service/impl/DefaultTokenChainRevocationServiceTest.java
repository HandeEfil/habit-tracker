package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import com.handegunaydin.habit_tracker.auth.repository.RefreshTokenRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultTokenChainRevocationServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private DefaultTokenChainRevocationService tokenChainRevocationService;


    @Test
    void refresh_whenReusedTokenHasMultiLevelChain_shouldRevokeOnlyChainEnd() {//failed
        RefreshToken chainA = tokenCreator("A");
        RefreshToken chainB = tokenCreator("B");
        RefreshToken chainC = tokenCreator("C");
        UUID UUID_A = UUID.randomUUID();
        UUID UUID_B = UUID.randomUUID();
        chainB.setReplacedByTokenId(UUID_A);
        chainC.setReplacedByTokenId(UUID_B);
        chainB.setRevoked(true);
        chainC.setRevoked(true);
        when(refreshTokenRepository.findRefreshTokenById(UUID_A)).thenReturn(Optional.of(chainA));
        when(refreshTokenRepository.findRefreshTokenById(UUID_B)).thenReturn(Optional.of(chainB));
        when(refreshTokenRepository.save(any())).thenReturn(tokenCreator("old-hash"));
        tokenChainRevocationService.detectRefreshTokenChain(chainC);
        verify(refreshTokenRepository, times(1)).save(chainA);
        verify(refreshTokenRepository, never()).save(chainB);
        verify(refreshTokenRepository, never()).save(chainC);

    }


    @Test
    void refresh_whenReusedTokenChainEndAlreadyRevoked_shouldNotCallSaveAgain() {
        RefreshToken chainA = tokenCreator("A");
        RefreshToken chainB = tokenCreator("B");
        RefreshToken chainC = tokenCreator("C");
        UUID UUID_A = UUID.randomUUID();
        UUID UUID_B = UUID.randomUUID();
        chainB.setReplacedByTokenId(UUID_A);
        chainC.setReplacedByTokenId(UUID_B);
        chainA.setRevoked(true);
        chainB.setRevoked(true);
        chainC.setRevoked(true);
        when(refreshTokenRepository.findRefreshTokenById(UUID_A)).thenReturn(Optional.of(chainA));
        when(refreshTokenRepository.findRefreshTokenById(UUID_B)).thenReturn(Optional.of(chainB));
        tokenChainRevocationService.detectRefreshTokenChain(chainC);
        verify(refreshTokenRepository, never()).save(chainA);

    }


    private static @NotNull RefreshToken tokenCreator(String hashToken) {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setTokenHashed(hashToken);
        oldToken.setEmail("test@test.com");
        oldToken.setExpiresAt(Instant.now().plusMillis(900000L));
        return oldToken;
    }
}
