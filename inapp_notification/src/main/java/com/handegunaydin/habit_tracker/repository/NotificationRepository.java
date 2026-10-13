package com.handegunaydin.habit_tracker.repository;

import com.handegunaydin.habit_tracker.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, UUID> {
    List<Notification> findByRecipient(String recipient);

    Optional<Notification> findByPublicIdAndRecipient(UUID id, String mail);
}
