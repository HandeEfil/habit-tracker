package com.handegunaydin.habit_tracker.consumer;

import com.handegunaydin.habit_tracker.dto.ChannelDistributorDomainEvent;
import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import com.handegunaydin.habit_tracker.repository.NotificationChannelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChannelDistributorEventListenerTest {
    @Mock
    private NotificationChannelRepository notificationChannelRepository;
    @Mock
    private KafkaTemplate<String, DomainEvent> template;
    @Mock
    private Environment environment;

    @InjectMocks
    private ChannelDistributorEventListener channelDistributorEventListener;

    @Test
    void consumeChannelsToBeInform_ChannelsShouldBeSentToRepository() {
        ArgumentCaptor<List<NotificationChannelType>> captorChannels = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<NotificationType> captorNotification = ArgumentCaptor.forClass(NotificationType.class);
        ChannelDistributorDomainEvent event = new ChannelDistributorDomainEvent(List.of(NotificationChannelType.EMAIL, NotificationChannelType.INAPP), NotificationType.WELCOME, null, UUID.randomUUID());
        channelDistributorEventListener.consumeChannelsToBeInform(event);
        verify(notificationChannelRepository).findNotificationChannelByEventTypeAndAllowedChannel(captorNotification.capture(), captorChannels.capture());
        assertEquals(event.getEventNotificationType(), captorNotification.getValue());
        assertEquals(event.allowedChannels(), captorChannels.getValue());
    }

    @Test
    void consumeChannelsToBeInform_whenChannelIsEmpty_EventShouldNotTrigger() {
        ChannelDistributorDomainEvent event = new ChannelDistributorDomainEvent(List.of(), NotificationType.WELCOME, null, UUID.randomUUID());
        when(notificationChannelRepository.findNotificationChannelByEventTypeAndAllowedChannel(any(), any())).thenReturn(List.of());
        channelDistributorEventListener.consumeChannelsToBeInform(event);
        verify(template, never()).send(any(), any());

    }

    @Test
    void consumeChannelsToBeInform_whenChannelIsNotEmpty_EventShouldTrigger() {
        ChannelDistributorDomainEvent event = new ChannelDistributorDomainEvent(List.of(NotificationChannelType.EMAIL, NotificationChannelType.INAPP), NotificationType.WELCOME, new UserFailedLoginAttemptEvent("Test", "test@test.com", NotificationType.WELCOME, UUID.randomUUID()), UUID.randomUUID());
        ArgumentCaptor<UserFailedLoginAttemptEvent> captorDataInapp = ArgumentCaptor.forClass(UserFailedLoginAttemptEvent.class);
        ArgumentCaptor<UserFailedLoginAttemptEvent> captorDataEmail = ArgumentCaptor.forClass(UserFailedLoginAttemptEvent.class);
        when(notificationChannelRepository.findNotificationChannelByEventTypeAndAllowedChannel(any(), any())).thenReturn(List.of(NotificationChannelType.EMAIL, NotificationChannelType.INAPP));
        when(environment.getProperty("kafka.message.email.template")).thenReturn("notification-events-mail");
        when(environment.getProperty("kafka.message.inapp.template")).thenReturn("notification-events-inapp");
        channelDistributorEventListener.consumeChannelsToBeInform(event);
        verify(environment, times(1)).getProperty("kafka.message.email.template");
        verify(environment, times(1)).getProperty("kafka.message.inapp.template");
        verify(template, times(1)).send(eq("notification-events-mail"), captorDataEmail.capture());
        verify(template, times(1)).send(eq("notification-events-inapp"), captorDataInapp.capture());
        assertEquals(event.eventData(), captorDataEmail.getValue());
        assertEquals(event.eventData(), captorDataInapp.getValue());

    }

}
