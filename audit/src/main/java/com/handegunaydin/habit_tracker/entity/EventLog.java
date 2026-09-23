package com.handegunaydin.habit_tracker.entity;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "security_event_log")
@Getter
@Setter
public class EventLog extends Item {
    @Id
    @GeneratedValue
    private long id;
    private String userId;

    @Enumerated(EnumType.STRING)
    private SecurityEventType eventType;
    private String ipAddress;
    private String userAgent;
    @Column(columnDefinition = "TEXT")
    private String metadata;


}
