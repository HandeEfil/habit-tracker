package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface DlqSuccessHandler {
    void onSuccess(ConsumerRecord<String, DomainEvent> record);
}
