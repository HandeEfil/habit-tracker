package com.handegunaydin.habit_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordDTO(
        @NotBlank(message = "password.blank.error")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$",
                message = "password.complexity.error"
        )
        String oldPassword,
        @NotBlank(message = "password.blank.error")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$",
                message = "password.complexity.error"
        )
        String newPassword
) {
}
