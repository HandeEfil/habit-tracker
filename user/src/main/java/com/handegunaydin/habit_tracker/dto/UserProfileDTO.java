package com.handegunaydin.habit_tracker.dto;

import java.time.LocalDate;

public record UserProfileDTO(
        String name,
        String mail,
        String mobileNumber,
        LocalDate birthDate
) {
}
