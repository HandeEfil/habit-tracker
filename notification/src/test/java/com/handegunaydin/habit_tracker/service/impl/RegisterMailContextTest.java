package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.dto.UserRegisterEvent;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class RegisterMailContextTest {

    private RegisterMailContext registerMailContext;

    @BeforeEach
    void setUp() {
        registerMailContext = new RegisterMailContext();
    }

    @Test
    void resolve_ShouldSetToAndUsernameVariables() {
        UserRegisterEvent event = new UserRegisterEvent("test", "test@test.com", NotificationType.WELCOME);
        Context registerContext = registerMailContext.resolve(event);
        assertEquals("test@test.com", registerContext.getVariable("to"));
        assertEquals("test", registerContext.getVariable("username"));

    }

    @Test
    void resolve_ShouldThrowClassCastException_WhenEventIsNotUserRegisterEvent() {

        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = mock(UserFailedLoginAttemptEvent.class);
        assertThrows(ClassCastException.class, () -> registerMailContext.resolve(userFailedLoginAttemptEvent));
    }

    @Test
    void supports_ShouldReturnTrue_ForUserRegisterEvent() {
        UserRegisterEvent event = mock(UserRegisterEvent.class);
        assertTrue(registerMailContext.supports(event));

    }

    @Test
    void supports_ShouldReturnFalse_ForOtherEventTypes() {
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = mock(UserFailedLoginAttemptEvent.class);
        assertFalse(registerMailContext.supports(userFailedLoginAttemptEvent));


    }
}
