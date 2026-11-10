package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.EmailMessage;

public interface EmailSender {
    void send(EmailMessage resolve);
}
