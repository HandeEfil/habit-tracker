package com.handegunaydin.habit_tracker.controller;

import com.handegunaydin.habit_tracker.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.service.UserService;
import jakarta.validation.Valid;
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

    @PreAuthorize("hasRole('CUSTOMER') and @defaultUserSecurityService.isOwner(#id, authentication.name)" )
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserDetails(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserDetails(id));

    }
    @PreAuthorize("hasRole('CUSTOMER') and @defaultUserSecurityService.isOwner(#profileDTO.mail(), authentication.name)" )
    @PutMapping("/update")
    public ResponseEntity<UserProfileDTO> getUserDetails(@Valid @RequestBody UserProfileDTO profileDTO) {
        return ResponseEntity.ok(userService.updateUser(profileDTO));

    }

}
