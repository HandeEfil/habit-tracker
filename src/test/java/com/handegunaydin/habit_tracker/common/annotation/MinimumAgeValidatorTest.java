package com.handegunaydin.habit_tracker.common.annotation;

import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinimumAgeValidatorTest {
    private MinimumAgeValidator minimumAgeValidator;

    @BeforeEach
    void setUp() {
        minimumAgeValidator = new MinimumAgeValidator();
        MinimumAge minimumAge = new MinimumAge(){

            @Override
            public Class<? extends Annotation> annotationType() {
                return MinimumAge.class;
            }

            @Override
            public int value() {
                return 18;
            }

            @Override
            public String message() {
                return "{user.age.minimum}";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }
        };
        minimumAgeValidator.initialize(minimumAge);

    }

    @Test
    void shouldReturnTrue_whenUserIsExactly18() {
        LocalDate birthdate = LocalDate.now().minusYears(18);
        boolean result = minimumAgeValidator.isValid(birthdate,null);
        assertTrue(result);

    }

    @Test
    void shouldReturnFalse_whenUserIsUnder18() {
        LocalDate birthDate = LocalDate.now().minusYears(17);
        boolean result = minimumAgeValidator.isValid(birthDate,null);
        assertFalse(result);
    }

    @Test
    void shouldReturnTrue_whenBirthDateIsNull() {
        boolean result = minimumAgeValidator.isValid(null, null);
        assertTrue(result); // since null control is done seperately.
    }
}
