package com.handegunaydin.habit_tracker.entity;

import com.handegunaydin.habit_tracker.enums.NotificationChannelType;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "notification_channel_configuration")
@Getter
@Setter
public class NotificationChannelConfiguration extends Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long ID;
    @Column(unique = true)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @ElementCollection
    @CollectionTable(
            name = "notification_channel_configuration_channels",
            joinColumns = @JoinColumn(name = "notification_channel_configuration_id")
    )
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private List<NotificationChannelType> notificationChannels;
}
