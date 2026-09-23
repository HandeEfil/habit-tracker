package com.handegunaydin.habit_tracker;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.entity.EventLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventLogMapper {

    @Mapping(target = "metadata", ignore = true)
    EventLog toEntity(SecurityEvent securityEvent);

}
