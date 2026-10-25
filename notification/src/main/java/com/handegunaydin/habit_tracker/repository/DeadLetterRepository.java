package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.DeadLetter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeadLetterRepository extends MongoRepository<DeadLetter, String> {
    Optional<DeadLetter> findByEventId(UUID eventId);

    @Query("{ 'status': { $in: ['NEW', 'RETRIED'] }, 'retryCount': { $lt: 3 } }")
    List<DeadLetter> findDLQsToRetry();
}
