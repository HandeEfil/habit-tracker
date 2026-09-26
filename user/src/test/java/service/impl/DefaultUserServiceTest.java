package service.impl;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.dto.UserProfileDTO;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.exception.UserNotFoundException;
import com.handegunaydin.habit_tracker.mapper.UserProfileMapper;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.impl.DefaultUserService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DefaultUserServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileMapper userProfileMapper;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @InjectMocks
    private DefaultUserService defaultUserService;

    @Test
    void getUserDetails_whenUserExists_returnsProfileDTO() {
        User user = getTestUser();
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(user));
        UserProfileDTO expectedProfileDTO = createProfileDto(user);
        when(userProfileMapper.toResponse(user)).thenReturn(expectedProfileDTO);
        UserProfileDTO userDetails = defaultUserService.getUserDetails("test@test.com");
        assertSame(expectedProfileDTO, userDetails);
        verify(userProfileMapper, times(1)).toResponse(user);

    }

    @Test
    void getUserDetails_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> defaultUserService.getUserDetails("test@test.com"));

    }

    @Test
    void updateUser_whenUserExists_updatesAndReturnsDTO() {
        successfullUpdate();
    }


    @Test
    void updateUser_whenUserExists_publishesProfileUpdatedEvent() {
        successfullUpdate();
        verify(applicationEventPublisher,times(1)).publishEvent(any(SecurityEvent.class));

    }

    @Test
    void updateUser_whenUserNotFound_throwsException() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> defaultUserService.updateUser(createProfileDto(getTestUser())));

    }

    @Test
    void updateUser_whenUserNotFound_doesNotPublishEvent() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> defaultUserService.updateUser(createProfileDto(getTestUser())));
        verify(applicationEventPublisher, never()).publishEvent(any(SecurityEvent.class));
    }


    private User getTestUser() {
        User user = new User();
        user.setMail("test@test.com");
        user.setName("Test Test");
        user.setMobileNumber("5555555555");
        user.setBirthDate(LocalDate.now());
        return user;
    }

    private void successfullUpdate() {
        User user = getTestUser();
        UserProfileDTO expectedProfileDTO = createProfileDto(user);
        UserProfileDTO updatedProfileDTO = new UserProfileDTO("Test Updated", user.getMail(), user.getMobileNumber(),null);
        User updatedUser = getTestUser();
        updatedUser.setName("Test Updated");
        updatedUser.setBirthDate(null);
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(user));
        when(userProfileMapper.toResponse(user)).thenReturn(expectedProfileDTO);
        when(userProfileMapper.toResponse(updatedUser)).thenReturn(updatedProfileDTO);
        when(userProfileMapper.updateEntityFromDto(any(),any())).thenReturn(updatedUser);
        when(userRepository.save(any())).thenReturn(updatedUser);
        UserProfileDTO userDetails = defaultUserService.updateUser(updatedProfileDTO);
        verify(userRepository,times(1)).save(any());
        assertSame(updatedProfileDTO, userDetails);
    }

    private @Nullable UserProfileDTO createProfileDto(User user) {
        return new UserProfileDTO(user.getName(), user.getMail(), user.getMobileNumber(), user.getBirthDate());

    }

}
