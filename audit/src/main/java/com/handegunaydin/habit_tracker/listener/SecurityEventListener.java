package com.handegunaydin.habit_tracker.listener;

import com.handegunaydin.habit_tracker.EventLogMapper;
import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.entity.EventLog;
import com.handegunaydin.habit_tracker.repository.SecurityEventRepository;
import com.handegunaydin.habit_tracker.service.ChangeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityEventListener {

    private final EventLogMapper eventLogMapper;
    private final SecurityEventRepository securityEventRepository;
    private final ChangeDetector changeDetector;

    @Async
    @EventListener
    public void handle(SecurityEvent securityEvent) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            EventLog entity = eventLogMapper.toEntity(securityEvent);

            // TODO: Consider replacing conditional event metadata handling with Strategy + Factory
            // when the number of event-specific metadata rules increases.
            if (securityEvent.source() != null && securityEvent.target() != null) {
                entity.setMetadata(objectMapper.writeValueAsString(changeDetector.changeDetector(securityEvent.source(), securityEvent.target())));
            } else if (securityEvent.metadata() != null) {
                entity.setMetadata(objectMapper.writeValueAsString(securityEvent.metadata()));
            }
            securityEventRepository.save(entity);

        } catch (
                Exception e) {
            log.error("An error occured while recording error : {}", securityEvent.toString());
        }

    }


}
