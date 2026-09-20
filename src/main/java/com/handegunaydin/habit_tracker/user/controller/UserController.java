package com.handegunaydin.habit_tracker.user.controller;

import com.handegunaydin.habit_tracker.user.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/users/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserDetails(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserDetails(id));

    }

}
