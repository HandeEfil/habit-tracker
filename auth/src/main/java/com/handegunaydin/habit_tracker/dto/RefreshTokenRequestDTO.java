package com.handegunaydin.habit_tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank
        String refreshToken
) {
}
