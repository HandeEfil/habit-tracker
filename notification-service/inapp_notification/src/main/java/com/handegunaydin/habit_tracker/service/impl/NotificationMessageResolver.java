package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMessageResolver {
    private final MessageSource messageSource;

    public String resolveMessage(DomainEvent event) {
        return messageSource.getMessage(event.getEventNotificationType().getMessageKey(), null, null);
    }
}
