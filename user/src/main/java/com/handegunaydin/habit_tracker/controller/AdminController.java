package com.handegunaydin.habit_tracker.controller;

import com.handegunaydin.habit_tracker.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")

public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());

    }
}
