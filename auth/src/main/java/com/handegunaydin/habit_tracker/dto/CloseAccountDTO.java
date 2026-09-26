package com.handegunaydin.habit_tracker.dto;

import jakarta.validation.constraints.NotBlank;

public record CloseAccountDTO(
        @NotBlank(message = "password.blank.error")
        String password
) {
}
