package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.dto.TokenPairResponseDTO;
import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import com.handegunaydin.habit_tracker.auth.jwt.JwtService;
import com.handegunaydin.habit_tracker.auth.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.auth.service.AuthService;
import com.handegunaydin.habit_tracker.auth.service.TokenChainRevocationService;
import com.handegunaydin.habit_tracker.auth.service.TokenGenerator;
import com.handegunaydin.habit_tracker.user.entity.User;
import com.handegunaydin.habit_tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DefaultAuthService implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenGenerator tokenGenerator;
    private final TokenChainRevocationService detectRefreshTokenChain;
    private final UserRepository userRepository;


    @Override
    public String generateRefreshToken(String mail) {
        String rawToken = tokenGenerator.generateRawToken();
        String tokenHash = tokenGenerator.getTokenHash(rawToken);
        RefreshToken refreshToken = tokenGenerator.populateHashedToken(mail, tokenHash);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }


    @Override
    @Transactional
    public TokenPairResponseDTO refresh(String rawOldRefreshToken) {
        String hashedOldRefreshToken = tokenGenerator.getTokenHash(rawOldRefreshToken);
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
        RefreshToken refreshTokenUpdated = tokenGenerator.populateHashedToken(oldRefreshToken.getEmail(), tokenHash);
        refreshTokenRepository.save(refreshTokenUpdated);
        oldRefreshToken.setRevoked(true);
        oldRefreshToken.setReplacedByTokenId(refreshTokenUpdated.getId());
        refreshTokenRepository.save(oldRefreshToken);
        User user = userRepository.findByMail(oldRefreshToken.getEmail()).orElseThrow( () -> new BadCredentialsException("User doesn't exist"));
        return new TokenPairResponseDTO(jwtService.generateToken(oldRefreshToken.getEmail(), user.getRoles()), rawToken);
    }

    @Override
    public void revokeAllForUser(String mail) {
        refreshTokenRepository.revokeAllForUser(mail);
    }


}
