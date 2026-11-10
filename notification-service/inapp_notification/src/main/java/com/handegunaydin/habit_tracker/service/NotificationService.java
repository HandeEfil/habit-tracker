package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.NotificationDTO;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void createNotification(DomainEvent event);

    List<NotificationDTO> findNotificationsByUser(String mail);

    void findNotificationsByPublicIDAndUser(UUID id, String mail);
}
