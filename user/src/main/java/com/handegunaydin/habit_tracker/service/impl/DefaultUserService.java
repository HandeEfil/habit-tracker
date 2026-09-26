package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import com.handegunaydin.habit_tracker.exception.UserNotFoundException;
import com.handegunaydin.habit_tracker.factory.SecurityEventFactory;
import com.handegunaydin.habit_tracker.mapper.UserProfileMapper;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

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

    @Override
    public UserProfileDTO updateUser(UserProfileDTO profileDTO) {
        User user = userRepository.findByMail(profileDTO.mail()).orElseThrow(() -> new UserNotFoundException("user.not.found", null));
        UserProfileDTO userProfileDTO = userProfileMapper.toResponse(user);
        User updatedUser = userRepository.save(userProfileMapper.updateEntityFromDto(profileDTO, user));
        UserProfileDTO updatedProfileDTO = userProfileMapper.toResponse(updatedUser);
        applicationEventPublisher.publishEvent(SecurityEventFactory.create(userProfileDTO.mail(), SecurityEventType.PROFILE_UPDATED, userProfileDTO, updatedProfileDTO, null));
        return updatedProfileDTO;
    }
}
