package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.DomainEvent;
import com.handegunaydin.habit_tracker.dto.NotificationDTO;
import com.handegunaydin.habit_tracker.entity.Notification;
import com.handegunaydin.habit_tracker.mapper.NotificationMapper;
import com.handegunaydin.habit_tracker.repository.NotificationRepository;
import com.handegunaydin.habit_tracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationMessageResolver notificationMessageResolver;

    @Override
    public void createNotification(DomainEvent event) {
        Notification notification = notificationMapper.fromEvent(event);
        notification.setMessage(notificationMessageResolver.resolveMessage(event));
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDTO> findNotificationsByUser(String mail) {
        List<Notification> notifications = notificationRepository.findByRecipient(mail);
        return notificationMapper.toResponseDTOList(notifications);
    }

    @Override
    public void findNotificationsByPublicIDAndUser(UUID id, String mail) {
        Optional<Notification> notification = notificationRepository.findByPublicIdAndRecipient(id, mail);
        notification.ifPresent(notificationUpdate -> {
            if (notificationUpdate.isRead()) {
                return;
            }
            notificationUpdate.setRead(true);
            notificationRepository.save(notificationUpdate);
        });
    }
}
