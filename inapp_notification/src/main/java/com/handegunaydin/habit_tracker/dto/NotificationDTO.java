package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.NotificationType;

import java.util.UUID;

public record NotificationDTO(
        UUID publicId,
        String recipient,
        String message,
        NotificationType notificationType,
        Boolean isRead
) {
}
