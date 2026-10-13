package com.handegunaydin.habit_tracker.dto;

public record EmailMessage(
        String from,
        String to,
        String subject,
        String body
) {
}
