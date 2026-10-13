package com.handegunaydin.habit_tracker.consumer;

import com.handegunaydin.habit_tracker.dto.ChannelDistributorDomainEvent;
import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.repository.NotificationChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelDistributorEventListener {
    private final NotificationChannelRepository notificationChannelRepository;
    private final KafkaTemplate<String, DomainEvent> template;

    private final Environment environment;

    @KafkaListener(topics = "${channel.distributor.event.topicId}", groupId = "${channel.distributor.event.groupId}")
    public void consumeChannelsToBeInform(ChannelDistributorDomainEvent channelDistributorDomainEvent) {
        List<NotificationChannelType> channels = channelDistributorDomainEvent.allowedChannels();
        List<NotificationChannelType> allowedChannelsToTrigger = notificationChannelRepository.findNotificationChannelByEventTypeAndAllowedChannel(channelDistributorDomainEvent.notificationType(), channels);
        if (CollectionUtils.isEmpty(allowedChannelsToTrigger)) {
            return;
        }
        allowedChannelsToTrigger.forEach(trigger -> template.send(environment.getProperty("kafka.message." + trigger.name().toLowerCase() + ".template"), channelDistributorDomainEvent.eventData()));

    }
}
