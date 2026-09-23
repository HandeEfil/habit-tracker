package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository extends JpaRepository<EventLog, Long> {
}
