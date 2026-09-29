package com.handegunaydin.habit_tracker.listener;

import com.handegunaydin.habit_tracker.EventLogMapper;
import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.entity.EventLog;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import com.handegunaydin.habit_tracker.repository.SecurityEventRepository;
import com.handegunaydin.habit_tracker.service.ChangeDetector;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityEventListenerTest {

    @Mock
    private EventLogMapper eventLogMapper;

    @Mock
    private SecurityEventRepository securityEventRepository;

    @Mock
    private ChangeDetector changeDetector;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SecurityEventListener securityEventListener;

    @Test
    void handle_ShouldMapSecurityEventToEntity_WhenEventIsReceived() {
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null)));
        verify(securityEventRepository, times(1)).save(any());

    }

    @Test
    void handle_ShouldSerializeMetadata_WhenMetadataIsProvided() {

        Map<String, Object> params = new HashMap<>();
        params.put("test-key", "test-value");
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, params)));
        verify(objectMapper, times(1)).writeValueAsString(any());

    }

    @Test
    void handle_ShouldDetectAndSerializeChanges_WhenSourceAndTargetAreProvided() {
        SecurityEvent source = new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null);
        SecurityEvent target = new SecurityEvent("test@test.com", SecurityEventType.PASSWORD_CHANGED, null, null, null, null, null);
        Map<String, Map<String, Object>> params = new HashMap<>();
        Map<String, Object> values = new HashMap<>();
        values.put("old", SecurityEventType.ACCOUNT_LOCKED);
        values.put("new", SecurityEventType.PASSWORD_CHANGED);
        params.put("eventType", values);
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(changeDetector.changeDetector(any(), any())).thenReturn(params);
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, source, target, null)));
        verify(objectMapper, times(1)).writeValueAsString(any());


    }

    @Test
    void handle_ShouldPreferChangeDetectorMetadata_WhenSourceTargetAndMetadataAreProvided() {
        SecurityEvent source = new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null);
        SecurityEvent target = new SecurityEvent("test@test.com", SecurityEventType.PASSWORD_CHANGED, null, null, null, null, null);
        Map<String, Map<String, Object>> params = new HashMap<>();
        Map<String, Object> values = new HashMap<>();
        values.put("old", SecurityEventType.ACCOUNT_LOCKED);
        values.put("new", SecurityEventType.PASSWORD_CHANGED);
        params.put("eventType", values);
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(changeDetector.changeDetector(any(), any())).thenReturn(params);
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, source, target, values)));
        verify(objectMapper, times(1)).writeValueAsString(params);
        verify(objectMapper, never()).writeValueAsString(values);
    }

    @Test
    void handle_ShouldNotThrowException_WhenRepositorySaveFails() {
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(securityEventRepository.save(any())).thenThrow(RuntimeException.class);
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null)));

    }

    @Test
    void handle_ShouldLogError_WhenRepositorySaveFails() {
        LogCaptor logCaptor = LogCaptor.forClass(SecurityEventListener.class);
        when(eventLogMapper.toEntity(any())).thenThrow(NullPointerException.class);
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null)));

        assertEquals(1, logCaptor.getErrorLogs().size());
    }

    @Test
    void handle_ShouldNotSaveEventLog_WhenMapperFails() {
        when(eventLogMapper.toEntity(any())).thenThrow(NullPointerException.class);
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null)));
        verify(securityEventRepository, never()).save(any());

    }

    @Test
    void handle_ShouldNotSaveEventLog_WhenMetadataSerializationFails() {
        Map<String, Object> params = new HashMap<>();
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(objectMapper.writeValueAsString(any())).thenThrow(JacksonException.class);
        securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, params));
        verify(securityEventRepository, never()).save(any());

    }

    @Test
    void handle_ShouldNotThrowException_WhenChangeDetectorFails() {
        Map<String, Object> params = new HashMap<>();
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(eventLogMapper.toEntity(any())).thenReturn(new EventLog());
        when(changeDetector.changeDetector(any(), any())).thenThrow(RuntimeException.class);
        assertDoesNotThrow(() -> securityEventListener.handle(new SecurityEvent("test@test.com", SecurityEventType.ACCOUNT_LOCKED, null, null, new Object(), new Object(), params)));
        verify(securityEventRepository, never()).save(any());

    }
}
