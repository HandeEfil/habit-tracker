package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.entity.DeadLetter;
import com.handegunaydin.habit_tracker.enums.RetryStatus;
import com.handegunaydin.habit_tracker.repository.DeadLetterRepository;
import com.handegunaydin.habit_tracker.service.DlqSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeadLetterSuccessHandler implements DlqSuccessHandler {
    private final DeadLetterRepository deadLetterRepository;

    @Override
    public void onSuccess(ConsumerRecord<String, DomainEvent> record) {
        Optional<DeadLetter> byEventId = deadLetterRepository.findByEventId(record.value().getUUID());
        if(byEventId.isPresent()){
            DeadLetter letter = byEventId.get();
            letter.setRetryStatus(RetryStatus.RESOLVED);
            deadLetterRepository.save(letter);
        }
    }
}
