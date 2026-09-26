package com.handegunaydin.habit_tracker.entity;

import com.handegunaydin.habit_tracker.document.BaseDocument;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "security_event_log")
@Getter
@Setter
public class EventLog extends BaseDocument {

    private String id;
    private String userId;
    private SecurityEventType eventType;
    private String ipAddress;
    private String userAgent;
    private String metadata;


}
