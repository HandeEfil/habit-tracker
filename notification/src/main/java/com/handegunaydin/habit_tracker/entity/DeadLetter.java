package com.handegunaydin.habit_tracker.entity;

import com.handegunaydin.habit_tracker.document.BaseDocument;
import com.handegunaydin.habit_tracker.enums.RetryStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Setter
@Getter
public class DeadLetter extends BaseDocument {
    @Id
    private String id;
    private UUID eventId;
    private String typeId;
    private String originalTopic;
    private long originalOffset;
    private int originalPartition;
    private String errorMsg;
    private int retryCount;
    private String exceptionClass;
    private RetryStatus retryStatus;
    private String event;


}
