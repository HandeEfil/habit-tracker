package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.UserLoginDTO;
import com.handegunaydin.habit_tracker.dto.UserLoginResponseDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterResponseDTO;
import com.handegunaydin.habit_tracker.exception.EmailAlreadyExistsException;
import com.handegunaydin.habit_tracker.exception.UserBlockedException;
import com.handegunaydin.habit_tracker.jwt.JwtService;
import com.handegunaydin.habit_tracker.mapper.UserMapper;
import com.handegunaydin.habit_tracker.service.AuthService;
import com.handegunaydin.habit_tracker.service.LoginAttemptService;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultLoginRegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;
    @Mock
    private AuthService authService;
    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private DefaultLoginRegisterService loginRegisterService;

    // Register tests
    @Test
    void shouldThrowException_whenEmailAlreadyExists() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("Test",
                "Test123.",
                "test@test.com",
                "5555555555", LocalDate.now().minusYears(18)
        );
        when(userRepository.existsByMail("test@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> loginRegisterService.register(userRegisterDTO));

    }

    @Test
    void shouldReturnSuccess_whenEmailIsUnique() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("Test",
                "Test123.",
                "test@test.com",
                "5555555555", LocalDate.now().minusYears(18)
        );
        when(userRepository.existsByMail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Test123.")).thenReturn("hashed_pass");
        User savedUser = new User();
        savedUser.setMail("test@test.com");
        when(userMapper.toEntity(userRegisterDTO)).thenReturn(savedUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(new UserRegisterResponseDTO("Test",
                "test@test.com",
                "5555555555", LocalDate.now().minusYears(18)));
        assertEquals(new UserRegisterResponseDTO("Test",
                "test@test.com",
                "5555555555", LocalDate.now().minusYears(18)), loginRegisterService.register(userRegisterDTO));

        verify(userRepository, times(1)).existsByMail(any(String.class));
    }

    @Test
    void shouldNotCall_whenEmailIsNotUnique() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("Test",
                "Test123.",
                "test@test.com",
                "5555555555", LocalDate.now().minusYears(18)
        );
        when(userRepository.existsByMail("test@test.com")).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class, () -> loginRegisterService.register(userRegisterDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    //login Tests

    @Test
    void shouldReturnToken_whenCredentialsAreValid() {
        UserLoginDTO userLoginDTO = new UserLoginDTO(
                "test@test.com", "Test123.");
        User user = new User();
        user.setEnabled(true);
        user.setEncodedPassword("Test123._hashed");
        when(userRepository.findByMail(userLoginDTO.mail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginDTO.password(), "Test123._hashed")).thenReturn(true);

        when(jwtService.generateToken(userLoginDTO.mail(), user.getRoles())).thenReturn("Test123._generated_token");
        when(authService.generateRefreshToken(userLoginDTO.mail())).thenReturn("Test123._generated_refresh_token");
        assertEquals(new UserLoginResponseDTO("test@test.com", "Test123._generated_token", "Test123._generated_refresh_token"), loginRegisterService.login(userLoginDTO));
    }

    @Test
    void shouldThrowException_whenUserNotFound() {
        UserLoginDTO userLoginDTO = new UserLoginDTO(
                "test@test.com", "Test123.");
        when(userRepository.findByMail(userLoginDTO.mail())).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.login(userLoginDTO));

    }

    @Test
    void shouldThrowException_whenPasswordIsWrong() {
        UserLoginDTO userLoginDTO = new UserLoginDTO(
                "test@test.com", "Test123.");
        User user = new User();
        user.setEnabled(true);
        user.setEncodedPassword("Test123._hashed");
        when(userRepository.findByMail(userLoginDTO.mail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginDTO.password(), "Test123._hashed")).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.login(userLoginDTO));

    }
    @Test
    void shouldThrowException_whenAccountIsLocked() {
        UserLoginDTO userLoginDTO = new UserLoginDTO(
                "test@test.com", "Test123.");
        User user = new User();
        user.setEncodedPassword("Test123._hashed");
        when(userRepository.findByMail(userLoginDTO.mail())).thenReturn(Optional.of(user));
        when(loginAttemptService.IsAccountLocked(userLoginDTO.mail())).thenReturn(true);
        assertThrows(UserBlockedException.class, () -> loginRegisterService.login(userLoginDTO));

    }

}
