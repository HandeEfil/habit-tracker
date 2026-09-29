package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.dto.RefreshTokenRequestDTO;
import com.handegunaydin.habit_tracker.dto.TokenPairResponseDTO;

import java.util.UUID;

public interface AuthService {
    String generateRefreshToken(String mail, UUID sessionId);

    TokenPairResponseDTO refresh(RefreshTokenRequestDTO rawRefreshToken);

    Integer revokeAllForUser(String mail);
}
