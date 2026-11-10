package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationMessageResolverTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private NotificationMessageResolver notificationMessageResolver;

    @Test
    void resolveMessage_ShouldReturnMessageFromMessageSource() {
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = new UserFailedLoginAttemptEvent("test", "test@test.com", NotificationType.FAILED_LOGIN, UUID.randomUUID());
        notificationMessageResolver.resolveMessage(userFailedLoginAttemptEvent);
        verify(messageSource).getMessage("notification.security_alert.message", null, null);

    }

    @Test
    void resolveMessage_ShouldThrowNoSuchMessageException_WhenKeyIsMissing() {
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = new UserFailedLoginAttemptEvent("test", "test@test.com", NotificationType.FAILED_LOGIN, UUID.randomUUID());
        when(messageSource.getMessage("notification.security_alert.message", null, null)).thenThrow(new NoSuchMessageException("error"));
        assertThrows(NoSuchMessageException.class, () -> notificationMessageResolver.resolveMessage(userFailedLoginAttemptEvent));

    }
}
