package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.dto.*;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.exception.EmailAlreadyExistsException;
import com.handegunaydin.habit_tracker.exception.UserBlockedException;
import com.handegunaydin.habit_tracker.jwt.JwtService;
import com.handegunaydin.habit_tracker.mapper.UserMapper;
import com.handegunaydin.habit_tracker.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.AuthService;
import com.handegunaydin.habit_tracker.service.LoginAttemptService;
import com.handegunaydin.habit_tracker.service.RedisTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RedisTokenService redisTokenService;
    @Mock
    ApplicationEventPublisher applicationEventPublisher;

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
        UUID sessionID = UUID.randomUUID();
        UserLoginDTO userLoginDTO = new UserLoginDTO(
                "test@test.com", "Test123.");
        User user = new User();
        user.setEnabled(true);
        user.setEncodedPassword("Test123._hashed");
        when(userRepository.findByMail(userLoginDTO.mail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginDTO.password(), "Test123._hashed")).thenReturn(true);

        when(jwtService.generateToken(eq(userLoginDTO.mail()), eq(user.getRoles()), any(UUID.class))).thenReturn("Test123._generated_token");
        when(authService.generateRefreshToken(eq(userLoginDTO.mail()), any(UUID.class))).thenReturn("Test123._generated_refresh_token");
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
    //logout tests

    @Test
    void shouldProvideParameter_whenUserLogout() {
        UUID sessionId = UUID.randomUUID();
        when(jwtService.extractSessionId(any())).thenReturn(sessionId.toString());
        doNothing().when(redisTokenService).disableAccessToken("access-token");
        loginRegisterService.logout("test@test.com", "access-token");
        verify(refreshTokenRepository, times(1)).revokeCurrentSession("test@test.com", sessionId);
        verify(redisTokenService, times(1)).disableAccessToken("access-token");
    }


    //logout all devices test

    @Test
    void shouldProvideParameter_whenUserLogoutAllDevices() {
        doNothing().when(authService).revokeAllForUser("test@test.com");
        doNothing().when(redisTokenService).disableAllAccessTokensForUser("test@test.com");
        loginRegisterService.logoutAllDevices("test@test.com");
        verify(authService, times(1)).revokeAllForUser("test@test.com");
        verify(redisTokenService, times(1)).disableAllAccessTokensForUser("test@test.com");
    }

    //change password

    @Test
    void changePassword_shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.empty());
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.changePassword(changePasswordDTO, "test@test.com", "access-token"));

    }

    @Test
    void changePassword_shouldPublishEvent_WhenPasswordMismatch() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.extractSessionId("access-token")).thenReturn("session-id");
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.changePassword(changePasswordDTO, "test@test.com", "access-token"));
        verify(applicationEventPublisher, times(1)).publishEvent(any(SecurityEvent.class));
        verify(userRepository, never()).save(any(User.class));


    }

    @Test
    void changePassword_shouldPublishEvent_WhenPasswordMatch() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.extractSessionId("access-token")).thenReturn("session-id");
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(refreshTokenRepository.revokeAllExceptCurrent(any(), any())).thenReturn(2);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        loginRegisterService.changePassword(changePasswordDTO, "test@test.com", "access-token");
        verify(applicationEventPublisher, times(1)).publishEvent(any(SecurityEvent.class));

    }

    @Test
    void changePassword_shouldSaveNewPassword_WhenPasswordMatch() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(jwtService.extractSessionId("access-token")).thenReturn("session-id");
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(refreshTokenRepository.revokeAllExceptCurrent(any(), any())).thenReturn(2);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        loginRegisterService.changePassword(changePasswordDTO, "test@test.com", "access-token");
        verify(applicationEventPublisher, times(1)).publishEvent(any(SecurityEvent.class));
        verify(userRepository, times(1)).save(any(User.class));

    }


    //close account tests
    @Test
    void closeAccount_shouldThrowException_WhenUserNotFound() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.closeAccount("test@test.com", "281501"));

    }

    @Test
    void closeAccount_shouldThrowException_WhenPasswordNotMatched() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.closeAccount("test@test.com", "281501"));

    }

    @Test
    void shouldNotRevokeTokens_WhenPasswordNotMatched() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.closeAccount("test@test.com", "281501"));
        verify(authService, never()).revokeAllForUser("test@test.com");
    }

    @Test
    void shouldNotSaveUser_WhenPasswordNotMatched() {
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> loginRegisterService.closeAccount("test@test.com", "281501"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldUpdateUser_WhenPasswordMatched() {
        User user = new User();
        user.setEnabled(true);
        user.setEncodedPassword("Test123._hashed");
        user.setMail("test@test.com");
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(userRepository.updateUserEnabled(any())).thenReturn(1);
        loginRegisterService.closeAccount("test@test.com", "281501");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertFalse(captor.getValue().isEnabled());
    }

    @Test
    void shouldTriggerRevokeAllForUser_WhenPasswordMatched() {
        User user = new User();
        user.setEnabled(true);
        user.setEncodedPassword("Test123._hashed");
        user.setMail("test@test.com");
        userRepository.save(user);
        when(userRepository.findByMail("test@test.com")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(userRepository.updateUserEnabled(any())).thenReturn(1);

        loginRegisterService.closeAccount("test@test.com", "281501");

        verify(authService, times(1)).revokeAllForUser("test@test.com");
    }

}
