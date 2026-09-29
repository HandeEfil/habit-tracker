package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.dto.*;
import jakarta.validation.Valid;

public interface LoginRegisterService {

    UserRegisterResponseDTO register(UserRegisterDTO user);

    UserLoginResponseDTO login(UserLoginDTO user);

    void closeAccount(String name, String password);

    void logout(String name, String accessToken);

    void logoutAllDevices(String name);

    void changePassword(@Valid ChangePasswordDTO changePasswordDTO, String mail, String accessToken);

    void resetPassword(String name);

    void resetPasswordRequest(String name);
}
