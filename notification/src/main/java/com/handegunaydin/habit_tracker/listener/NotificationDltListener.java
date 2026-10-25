package com.handegunaydin.habit_tracker.listener;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.entity.DeadLetter;
import com.handegunaydin.habit_tracker.enums.RetryStatus;
import com.handegunaydin.habit_tracker.repository.DeadLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationDltListener {
    private final DeadLetterRepository deadLetterRepository;
    private final ObjectMapper objectMapper;
    @Value("${kafka.message.dlq.template}")
    private String topic;

    @KafkaListener(topics = "${kafka.message.dlq.template}", groupId = "${kafka.message.dlq.groupId}")
    void listen(DomainEvent event, @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String errorMsg,
                @Header(KafkaHeaders.DLT_EXCEPTION_STACKTRACE) String stackTrace,
                @Header(KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN) String exceptionClass,
                @Header(KafkaHeaders.DLT_ORIGINAL_PARTITION) int originalPartition,
                @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset,
                @Header(value = "__TypeId__", required = false) String typeId) {

        UUID eventId = event.getUUID();
        Optional<DeadLetter> optEvent = deadLetterRepository.findByEventId(eventId);
        if(optEvent.isPresent()){
            DeadLetter eventToUpdate = optEvent.get();
            if(eventToUpdate.getRetryCount() <3){
                eventToUpdate.setRetryCount(eventToUpdate.getRetryCount()+1);
                eventToUpdate.setRetryStatus(RetryStatus.RETRIED);
                if(eventToUpdate.getRetryCount() == 3) {
                    eventToUpdate.setRetryStatus(RetryStatus.MANUAL_ACTION_NEEDED);
                }
                deadLetterRepository.save(eventToUpdate);
            }

        }else {

            DeadLetter deadLetter = new DeadLetter();
            deadLetter.setOriginalTopic(topic);
            deadLetter.setOriginalTopic(originalTopic);
            deadLetter.setOriginalOffset(originalOffset);
            deadLetter.setOriginalPartition(originalPartition);
            deadLetter.setErrorMsg(errorMsg);
            deadLetter.setRetryCount(0);
            deadLetter.setRetryStatus(RetryStatus.NEW);
            deadLetter.setExceptionClass(exceptionClass);
            deadLetter.setEvent(objectMapper.writeValueAsString(event));
            deadLetterRepository.save(deadLetter);
        }

    }
}
