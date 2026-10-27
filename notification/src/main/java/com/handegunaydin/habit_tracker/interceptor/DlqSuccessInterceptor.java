package com.handegunaydin.habit_tracker.interceptor;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.service.DlqSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DlqSuccessInterceptor implements RecordInterceptor<String, DomainEvent> {
    private final DlqSuccessHandler dlqSuccessHandler;

    @Override
    public @Nullable ConsumerRecord<String, DomainEvent> intercept(ConsumerRecord<String, DomainEvent> record, Consumer<String, DomainEvent> consumer) {
        return record;
    }

    @Override
    public void success(ConsumerRecord<String, DomainEvent> record, Consumer<String, DomainEvent> consumer) {
        dlqSuccessHandler.onSuccess(record);
    }
}
