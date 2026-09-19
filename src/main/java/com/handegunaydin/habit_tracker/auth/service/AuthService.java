package com.handegunaydin.habit_tracker.auth.service;

import com.handegunaydin.habit_tracker.auth.dto.TokenPairResponseDTO;

public interface AuthService {
    String generateRefreshToken(String mail);
    TokenPairResponseDTO refresh(String rawRefreshToken);
    void revokeAllForUser(String mail);
}
