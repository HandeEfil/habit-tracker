package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.NotificationType;

public record UserRegisterEvent(
        String name,
        String mail,
        NotificationType notificationType
) implements DomainEvent {

    @Override
    public NotificationType getEventNotificationType() {
        return notificationType;
    }

}
