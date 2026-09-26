package com.handegunaydin.habit_tracker.service;

import java.util.Map;

public interface ChangeDetector {
    Map<String, Map<String, Object>> changeDetector(Object source, Object target);
}
