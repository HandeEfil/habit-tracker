package com.handegunaydin.habit_tracker.listener;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InAppListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.message.inapp.template}", groupId = "${consumer.message.inapp.groupId}")
    public void inAppSender(DomainEvent event) {
        notificationService.createNotification(event);
    }
}
