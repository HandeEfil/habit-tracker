package com.handegunaydin.habit_tracker.dto;

public record UserLoginResponseDTO(
        String mail,
        String token,
        String refreshToken
) {
}
