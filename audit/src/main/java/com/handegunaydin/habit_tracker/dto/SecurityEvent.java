package com.handegunaydin.habit_tracker.dto;

import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Map;

public record SecurityEvent(
        String userId,
        @Enumerated(EnumType.STRING)
        SecurityEventType eventType,
        String ipAddress,
        String userAgent,
        Object source,
        Object target,
        Map<String, Object> metadata
) {
}
