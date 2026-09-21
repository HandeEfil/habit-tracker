package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.exception.UserNotFoundException;
import com.handegunaydin.habit_tracker.mapper.UserProfileMapper;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfileDTO getUserDetails(String email) {
        User user = userRepository.findByMail(email).orElseThrow(() -> new UserNotFoundException("user.not.found", null));
        return userProfileMapper.toResponse(user);
    }

    @Override
    public List<UserProfileDTO> getUsers() {
        List<User> user = userRepository.findAll();
        return userProfileMapper.toResponseList(user);
    }
}
