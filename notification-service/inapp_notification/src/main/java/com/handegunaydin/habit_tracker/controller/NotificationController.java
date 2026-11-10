package com.handegunaydin.habit_tracker.controller;

import com.handegunaydin.habit_tracker.dto.NotificationDTO;
import com.handegunaydin.habit_tracker.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize(value = "hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication auth) {
        List<NotificationDTO> notificationsByUser = notificationService.findNotificationsByUser(auth.getName());
        return ResponseEntity.ok(notificationsByUser);
    }

    @PreAuthorize(value = "hasRole('CUSTOMER')")
    @PatchMapping(value = "{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, Authentication auth) {
        notificationService.findNotificationsByPublicIDAndUser(id, auth.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
