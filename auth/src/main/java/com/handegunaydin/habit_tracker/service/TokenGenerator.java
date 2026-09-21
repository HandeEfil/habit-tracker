package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.entity.RefreshToken;

public interface TokenGenerator {
    RefreshToken populateHashedToken(String mail, String tokenHash);
    String getTokenHash(String rawToken);
    String generateRawToken();

}
