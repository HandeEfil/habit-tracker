package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.entity.RefreshToken;

import java.util.UUID;

public interface TokenGenerator {
    RefreshToken populateHashedToken(String mail, String tokenHash, UUID sessionId);

    String getTokenHash(String rawToken);

    String generateRawToken();

}
