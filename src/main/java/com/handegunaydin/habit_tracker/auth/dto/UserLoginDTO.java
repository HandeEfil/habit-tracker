package com.handegunaydin.habit_tracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginDTO(


        @Email(message = "mail.format.error")
        @NotBlank(message = "mail.blank.error")
        String mail,

        @NotBlank(message = "password.blank.error")
        String password
        ) {
}
