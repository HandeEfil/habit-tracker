package com.handegunaydin.habit_tracker.auth.dto;

public record UserLoginResponseDTO(
        String mail,
        String token
) {
}
