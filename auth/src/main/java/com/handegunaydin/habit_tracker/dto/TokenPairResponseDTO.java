package com.handegunaydin.habit_tracker.dto;

public record TokenPairResponseDTO(
        String accessToken,
        String refreshToken
) {
}
