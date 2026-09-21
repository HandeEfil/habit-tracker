package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.dto.UserLoginDTO;
import com.handegunaydin.habit_tracker.dto.UserLoginResponseDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterResponseDTO;

public interface LoginRegisterService {

    UserRegisterResponseDTO register(UserRegisterDTO user);
    UserLoginResponseDTO login(UserLoginDTO user);
}
