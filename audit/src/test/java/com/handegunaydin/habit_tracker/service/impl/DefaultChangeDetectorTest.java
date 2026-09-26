package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class DefaultChangeDetectorTest {

    @InjectMocks
    private DefaultChangeDetector changeDetector;

    @Test
    void changeDetector_whenDifferentClasses_returnsEmptyMap () {
        Map<String, Map<String, Object>> changeDetectorMap = changeDetector.changeDetector(new SecurityEvent("test@test.com",
                SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null), new Object());

        assertTrue(changeDetectorMap.isEmpty());

    }

    @Test
    void changeDetector_whenNoFieldsDiffer_returnsEmptyMap(){
        Map<String, Map<String, Object>> changeDetectorMap = changeDetector.changeDetector(
                new SecurityEvent("test@test.com",
                SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null),
  new SecurityEvent("test@test.com",
                SecurityEventType.ACCOUNT_LOCKED, null, null, null, null, null));

        assertTrue(changeDetectorMap.isEmpty());

    }

    @Test
    void changeDetector_whenFieldsDiffer_returnsOldAndNewValues() {
        Map<String, Map<String, Object>> changeDetectorMap = changeDetector.changeDetector(
                new SecurityEvent("test@test.com",
                        SecurityEventType.ACCOUNT_LOCKED, "ip1", "mobile", null, null, null),
                new SecurityEvent("test12@test.com",
                        SecurityEventType.ACCOUNT_LOCKED, "ip2", null, null, new Object(), null));

        assertTrue(changeDetectorMap.size() == 4);

    }

}
