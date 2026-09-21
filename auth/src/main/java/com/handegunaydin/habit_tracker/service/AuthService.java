package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.dto.TokenPairResponseDTO;

public interface AuthService {
    String generateRefreshToken(String mail);
    TokenPairResponseDTO refresh(String rawRefreshToken);
    void revokeAllForUser(String mail);
}
