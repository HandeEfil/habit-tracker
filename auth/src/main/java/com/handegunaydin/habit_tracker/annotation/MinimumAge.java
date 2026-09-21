package com.handegunaydin.habit_tracker.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumAgeValidator.class)
public @interface MinimumAge {
    int value() default 18;
    String message() default "{user.age.minimum}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
