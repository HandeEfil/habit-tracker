package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.NotificationDTO;
import com.handegunaydin.habit_tracker.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.entity.FailedLoginEventNotification;
import com.handegunaydin.habit_tracker.entity.Notification;
import com.handegunaydin.habit_tracker.entity.RegisterEventNotification;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import com.handegunaydin.habit_tracker.mapper.NotificationMapper;
import com.handegunaydin.habit_tracker.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.NoSuchMessageException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationMessageResolver notificationMessageResolver;

    @InjectMocks
    private DefaultNotificationService notificationService;

    @Test
    void createNotification_shouldMapResolveMessageAndSave() {
        ArgumentCaptor<FailedLoginEventNotification> captor = ArgumentCaptor.forClass(FailedLoginEventNotification.class);
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = new UserFailedLoginAttemptEvent("Test", "test@test.com", NotificationType.FAILED_LOGIN);
        FailedLoginEventNotification notification = new FailedLoginEventNotification();
        when(notificationMapper.fromEvent(userFailedLoginAttemptEvent)).thenReturn(notification);
        when(notificationMessageResolver.resolveMessage(userFailedLoginAttemptEvent)).thenReturn("resolved-message");
        notificationService.createNotification(userFailedLoginAttemptEvent);
        verify(notificationRepository).save(captor.capture());
    }

    @Test
    void createNotification_ifMessageResolverThrows_shouldNotSave() {//log with audit.
        UserFailedLoginAttemptEvent userFailedLoginAttemptEvent = new UserFailedLoginAttemptEvent("Test", "test@test.com", NotificationType.FAILED_LOGIN);
        FailedLoginEventNotification notification = new FailedLoginEventNotification();
        when(notificationMapper.fromEvent(userFailedLoginAttemptEvent)).thenReturn(notification);
        when(notificationMessageResolver.resolveMessage(userFailedLoginAttemptEvent)).thenThrow(new NoSuchMessageException("no such element"));
        assertThrows(NoSuchMessageException.class, () -> notificationService.createNotification(userFailedLoginAttemptEvent));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void findNotificationsByUser_shouldReturnMappedDTOs() {
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        List<Notification> notificationList = List.of(new FailedLoginEventNotification(), new RegisterEventNotification());
        when(notificationRepository.findByRecipient(any())).thenReturn(notificationList);
        List<NotificationDTO> dtoList = List.of(
                new NotificationDTO(UUID.randomUUID(), "test", "msg", NotificationType.FAILED_LOGIN, false),
                new NotificationDTO(UUID.randomUUID(), "test1", "msg", NotificationType.FAILED_LOGIN, false));
        when(notificationMapper.toResponseDTOList(notificationList)).thenReturn(dtoList);
        List<NotificationDTO> notificationsByUser = notificationService.findNotificationsByUser("test");
        assertEquals(dtoList, notificationsByUser);

    }

    @Test
    void findNotificationsByUser_ifNoNotifications_shouldReturnEmptyList() {
        when(notificationRepository.findByRecipient(any())).thenReturn(Collections.EMPTY_LIST);
        when(notificationMapper.toResponseDTOList(Collections.EMPTY_LIST)).thenReturn(Collections.EMPTY_LIST);
        List<NotificationDTO> notificationsByUser = notificationService.findNotificationsByUser("test");
        assertEquals(Collections.emptyList(), notificationsByUser);
    }

    @Test
    void findNotificationsByPublicIDAndUser_ifFound_shouldMarkAsReadAndSave() {
        ArgumentCaptor<FailedLoginEventNotification> captor = ArgumentCaptor.forClass(FailedLoginEventNotification.class);
        FailedLoginEventNotification notification = new FailedLoginEventNotification();
        when(notificationRepository.findByPublicIdAndRecipient(any(), any())).thenReturn(Optional.of(notification));
        assertFalse(notification.isRead());
        notificationService.findNotificationsByPublicIDAndUser(UUID.randomUUID(), "test@test.com");
        verify(notificationRepository).save(captor.capture());
        assertTrue(captor.getValue().isRead());


    }

    @Test
    void findNotificationsByPublicIDAndUser_ifNotFound_shouldNotSave() {
        when(notificationRepository.findByPublicIdAndRecipient(any(), any())).thenReturn(Optional.empty());
        notificationService.findNotificationsByPublicIDAndUser(UUID.randomUUID(), "test@test.com");
        verify(notificationRepository, never()).save(any());

    }

    @Test
    void findNotificationsByPublicIDAndUser_ifAlreadyRead_shouldNotSave() {
        FailedLoginEventNotification notification = new FailedLoginEventNotification();
        notification.setRead(true);
        when(notificationRepository.findByPublicIdAndRecipient(any(), any())).thenReturn(Optional.of(notification));
        notificationService.findNotificationsByPublicIDAndUser(UUID.randomUUID(), "test@test.com");
        verify(notificationRepository, never()).save(any());

    }


}
