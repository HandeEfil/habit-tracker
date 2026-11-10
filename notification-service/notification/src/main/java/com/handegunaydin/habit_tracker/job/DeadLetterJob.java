package com.handegunaydin.habit_tracker.job;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.entity.DeadLetter;
import com.handegunaydin.habit_tracker.repository.DeadLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadLetterJob {

    private final DeadLetterRepository deadLetterRepository;
    private final KafkaTemplate<String, DomainEvent> template;


    @Scheduled(cron = "0 30 9 * * *")
    public void redrive() {
        List<DeadLetter> deadLetters = deadLetterRepository.findDLQsToRetry();
        ObjectMapper objectMapper = new ObjectMapper();
        deadLetters.forEach(letter -> {
            try {
                template.send(letter.getOriginalTopic(), objectMapper.readValue(letter.getEvent(), (Class<DomainEvent>) Class.forName(letter.getExceptionClass())));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });


    }
}
