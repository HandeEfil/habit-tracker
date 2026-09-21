package com.handegunaydin.habit_tracker.dto;

import java.time.LocalDate;

public record UserRegisterResponseDTO(

        String name,
        String mail,
        String mobileNumber,
        LocalDate birthDate

) {

}
