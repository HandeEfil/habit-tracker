package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.NotificationType;

import java.util.UUID;

public record UserFailedLoginAttemptEvent(
        String name,
        String mail,
        NotificationType notificationType,
        UUID uuid
) implements DomainEvent {


    @Override
    public NotificationType getEventNotificationType() {
        return notificationType;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
