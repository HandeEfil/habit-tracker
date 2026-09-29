package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.RefreshTokenRequestDTO;
import com.handegunaydin.habit_tracker.dto.TokenPairResponseDTO;
import com.handegunaydin.habit_tracker.entity.RefreshToken;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.jwt.JwtService;
import com.handegunaydin.habit_tracker.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.AuthService;
import com.handegunaydin.habit_tracker.service.TokenChainRevocationService;
import com.handegunaydin.habit_tracker.service.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final TokenChainRevocationService detectRefreshTokenChain;
    private final UserRepository userRepository;


    @Override
    public String generateRefreshToken(String mail, UUID sessionId) {
        String rawToken = tokenGenerator.generateRawToken();
        String tokenHash = tokenGenerator.getTokenHash(rawToken);
        RefreshToken refreshToken = tokenGenerator.populateHashedToken(mail, tokenHash, sessionId);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }


    @Override
    @Transactional
    public TokenPairResponseDTO refresh(RefreshTokenRequestDTO requestDTO) {
        String hashedOldRefreshToken = tokenGenerator.getTokenHash(requestDTO.refreshToken());
        RefreshToken oldRefreshToken = refreshTokenRepository.findRefreshTokenByTokenHashed(hashedOldRefreshToken).orElseThrow(() -> new BadCredentialsException("UPDATE"));

        if (oldRefreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("timeout token expired");

        }
        int isRevoked = refreshTokenRepository.revokeIfActive(hashedOldRefreshToken);
        if (isRevoked == 0) {
            detectRefreshTokenChain.detectRefreshTokenChain(oldRefreshToken);
            throw new BadCredentialsException("already revoked");
        }
        String rawToken = tokenGenerator.generateRawToken();
        String tokenHash = tokenGenerator.getTokenHash(rawToken);
        RefreshToken refreshTokenUpdated = tokenGenerator.populateHashedToken(oldRefreshToken.getEmail(), tokenHash, oldRefreshToken.getSessionId());
        refreshTokenRepository.save(refreshTokenUpdated);
        oldRefreshToken.setRevoked(true);
        oldRefreshToken.setReplacedByTokenId(refreshTokenUpdated.getId());
        refreshTokenRepository.save(oldRefreshToken);
        User user = userRepository.findByMail(oldRefreshToken.getEmail()).orElseThrow(() -> new BadCredentialsException("User doesn't exist"));
        return new TokenPairResponseDTO(jwtService.generateToken(oldRefreshToken.getEmail(), user.getRoles(), refreshTokenUpdated.getSessionId()), rawToken);
    }

    @Override
    public Integer revokeAllForUser(String mail) {
        return refreshTokenRepository.revokeAllForUser(mail);
    }

}
