package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.enums.NotificationType;

import java.util.List;

public record ChannelDistributorDomainEvent<T extends DomainEvent>(
        List<NotificationChannelType> allowedChannels,
        NotificationType notificationType,
        T eventData

) implements DomainEvent {

    @Override
    public NotificationType getEventNotificationType() {
        return notificationType;
    }
}
