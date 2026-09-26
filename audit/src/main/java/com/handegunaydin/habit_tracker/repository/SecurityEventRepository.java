package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.EventLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository extends MongoRepository<EventLog, String> {
}
