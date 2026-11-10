package com.handegunaydin.habit_tracker.entity;

import com.handegunaydin.habit_tracker.document.BaseDocument;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "inapp_notification")
@Getter
@Setter
public abstract class Notification extends BaseDocument {
    @Id
    private String ID;
    private UUID publicId = UUID.randomUUID();
    private String recipient;
    private String message;
    private NotificationType notificationType;
    private boolean isRead = false;


}
