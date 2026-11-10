package com.handegunaydin.habit_tracker.listener;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.EmailMessage;
import com.handegunaydin.habit_tracker.service.EmailResolver;
import com.handegunaydin.habit_tracker.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailListener {

    private final EmailSender emailSender;
    private final EmailResolver<DomainEvent> emailResolver;

    @KafkaListener(topics = "${kafka.message.email.template}", groupId = "${consumer.message.email.groupId}")
    public void mailSender(DomainEvent event) {
        EmailMessage resolve = emailResolver.resolve(event);
        emailSender.send(resolve);
    }
}
