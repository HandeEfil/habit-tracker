package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.service.ChangeDetector;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class DefaultChangeDetector implements ChangeDetector {
    @Override
    public Map<String, Map<String, Object>> changeDetector(Object source, Object target) {
        Map<String, Map<String, Object>> changeDetectorMap = new HashMap<>();
        if (!source.getClass().equals(target.getClass())) {
            return changeDetectorMap;
        }
        Arrays.stream(source.getClass().getDeclaredFields()).forEach(field -> {
            Map<String, Object> valueMap = new HashMap<>();
            field.setAccessible(true);
            try {
                if (!Objects.equals(field.get(source), field.get(target))) {
                    valueMap.put("old", field.get(source));
                    valueMap.put("new", field.get(target));
                    changeDetectorMap.put(field.getName(), valueMap);

                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        });

        return changeDetectorMap;
    }
}

