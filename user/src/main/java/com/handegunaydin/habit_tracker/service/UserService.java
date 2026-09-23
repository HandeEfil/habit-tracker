package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.dto.UserProfileDTO;

import java.util.List;

public interface UserService {
    UserProfileDTO getUserDetails(String email);
    List<UserProfileDTO> getUsers();
    UserProfileDTO updateUser(UserProfileDTO profileDTO);
}
