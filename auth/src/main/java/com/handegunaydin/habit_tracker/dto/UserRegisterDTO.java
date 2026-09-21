package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.annotation.MinimumAge;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRegisterDTO(
        @NotBlank(message = "name.blank.error")
        String name,

        @NotBlank(message = "password.blank.error")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$",
                message = "password.complexity.error"
        )
        String password,

        @Email(message = "mail.format.error")
        @NotBlank(message = "mail.blank.error")
        String mail,

        @NotBlank(message = "number.blank.error")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "number.format.error"
        )
        String mobileNumber,

        @NotNull(message =  "birthDate.blank.error")
        @Past(message = "{birthDate.past.error}")
        @MinimumAge
        LocalDate birthDate
        ) {
}
