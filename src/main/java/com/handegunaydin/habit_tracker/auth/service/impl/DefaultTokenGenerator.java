package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;
import com.handegunaydin.habit_tracker.auth.service.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import static java.time.Instant.now;

@Service
@RequiredArgsConstructor
public class DefaultTokenGenerator implements TokenGenerator {

    @Value("${refresh.token.expiration}")
    private long expirationDurationForRefreshToken;

    @Override
    public RefreshToken populateHashedToken(String mail, String tokenHash) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHashed(tokenHash);
        Instant now = now();
        refreshToken.setCreatedAt(now);
        refreshToken.setEmail(mail);
        refreshToken.setExpiresAt(now.plusMillis(expirationDurationForRefreshToken));
        return refreshToken;
    }

    @Override
    public String getTokenHash(String rawToken) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hashBytes = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);

    }

    @Override
    public String generateRawToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

    }
}
