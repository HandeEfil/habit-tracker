package com.handegunaydin.habit_tracker.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.handegunaydin.habit_tracker.enums.NotificationType;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = UserFailedLoginAttemptEvent.class,
                name = "FAILED_LOGIN"
        ),
        @JsonSubTypes.Type(
                value = UserRegisterEvent.class,
                name = "REGISTER"
        )
})
public interface DomainEvent {

    NotificationType getEventNotificationType();

    UUID getUUID();

}
