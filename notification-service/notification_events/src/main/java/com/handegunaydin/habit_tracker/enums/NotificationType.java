package com.handegunaydin.habit_tracker.enums;

public enum NotificationType {
    WELCOME("notification.welcome.message"),
    FAILED_LOGIN("notification.security_alert.message");
    private final String messageKey;

    NotificationType(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
