package com.handegunaydin.habit_tracker.auth.service;

import com.handegunaydin.habit_tracker.auth.dto.UserLoginDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserLoginResponseDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterResponseDTO;

public interface UserService {

    UserRegisterResponseDTO register(UserRegisterDTO user);
    UserLoginResponseDTO login(UserLoginDTO user);
}
