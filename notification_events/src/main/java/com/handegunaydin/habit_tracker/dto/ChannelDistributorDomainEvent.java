package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public record ChannelDistributorDomainEvent<T extends DomainEvent>(
        List<NotificationChannelType> allowedChannels,
        NotificationType notificationType,
        T eventData,
        UUID event_id

) implements DomainEvent {

    @Override
    public NotificationType getEventNotificationType() {
        return notificationType;
    }

    @Override
    public UUID getUUID() {
        return event_id;
    }
}
