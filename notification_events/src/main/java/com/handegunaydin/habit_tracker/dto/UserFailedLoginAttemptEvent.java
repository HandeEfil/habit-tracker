package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.NotificationType;

public record UserFailedLoginAttemptEvent(
        String name,
        String mail,
        NotificationType notificationType
) implements DomainEvent {


    @Override
    public NotificationType getEventNotificationType() {
        return notificationType;
    }

}
