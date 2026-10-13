package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.dto.ChannelDistributorDomainEvent;
import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChannelDistributorEventProducerTest {
    private final String topic = "test-topic";
    @Mock
    private KafkaTemplate<String, DomainEvent> template;
    @InjectMocks
    private ChannelDistributorEventProducer channelDistributorEventProducer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(channelDistributorEventProducer, "topic", topic);
    }

    @Test
    void publishChannelDistributorEvent_shouldSendEventToKafka() {
        ChannelDistributorDomainEvent event = new ChannelDistributorDomainEvent<>(List.of(NotificationChannelType.INAPP, NotificationChannelType.EMAIL), NotificationType.FAILED_LOGIN, null);
        channelDistributorEventProducer.publishChannelDistributorEvent(event);
        verify(template, times(1)).send(any(), eq(event));

    }

    @Test
    void publishChannelDistributorEvent_shouldUseConfiguredTopic() {
        ChannelDistributorDomainEvent event = new ChannelDistributorDomainEvent<>(List.of(NotificationChannelType.INAPP, NotificationChannelType.EMAIL), NotificationType.FAILED_LOGIN, null);
        channelDistributorEventProducer.publishChannelDistributorEvent(event);
        verify(template, times(1)).send(topic, event);

    }

    @Test
    void publishChannelDistributorEvent_shouldLogErrorWhenPublishingEventFails() {
        LogCaptor logCaptor = LogCaptor.forClass(ChannelDistributorEventProducer.class);
        ChannelDistributorDomainEvent event = new ChannelDistributorDomainEvent<>(List.of(NotificationChannelType.INAPP, NotificationChannelType.EMAIL), NotificationType.FAILED_LOGIN, null);
        CompletableFuture<SendResult<String, DomainEvent>> future = new CompletableFuture<>();
        when(template.send(any(), any())).thenReturn(future);
        channelDistributorEventProducer.publishChannelDistributorEvent(event);
        future.completeExceptionally(new RuntimeException("Kafka Error"));
        assertEquals(1, logCaptor.getErrorLogs().size());


    }
}
