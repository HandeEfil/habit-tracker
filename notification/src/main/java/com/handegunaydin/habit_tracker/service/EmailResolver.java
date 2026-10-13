package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.EmailMessage;

public interface EmailResolver<T extends DomainEvent> {
    EmailMessage resolve(T event);
}
