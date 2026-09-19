package com.handegunaydin.habit_tracker.auth.dto;

public record TokenPairResponseDTO(
        String accessToken,
        String refreshToken
) {
}
