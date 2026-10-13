package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.ChannelDistributorDomainEvent;
import com.handegunaydin.habit_tracker.dto.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChannelDistributorEventProducer {

    private final KafkaTemplate<String, DomainEvent> template;
    @Value("${channel.distributor.event.topicId}")
    private String topic;

    public void publishChannelDistributorEvent(ChannelDistributorDomainEvent event) {
        template.send(topic, event)
                .whenComplete(((stringDomainEventSendResult, throwable) -> {
                    if (throwable != null) {
                        log.error("An error occured while sending UserRegisterEvent : {}", event.toString());
                    }
                }));
    }
}
