package com.handegunaydin.habit_tracker.service.impl;


import com.handegunaydin.habit_tracker.dto.*;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import com.handegunaydin.habit_tracker.exception.EmailAlreadyExistsException;
import com.handegunaydin.habit_tracker.exception.UserBlockedException;
import com.handegunaydin.habit_tracker.factory.SecurityEventFactory;
import com.handegunaydin.habit_tracker.jwt.JwtService;
import com.handegunaydin.habit_tracker.mapper.UserMapper;
import com.handegunaydin.habit_tracker.repository.RefreshTokenRepository;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.AuthService;
import com.handegunaydin.habit_tracker.service.LoginAttemptService;
import com.handegunaydin.habit_tracker.service.LoginRegisterService;
import com.handegunaydin.habit_tracker.service.RedisTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultLoginRegisterService implements LoginRegisterService {


    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final AuthService authService;
    private final RedisTokenService redisTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public UserRegisterResponseDTO register(UserRegisterDTO registerRequest) {
        if (userRepository.existsByMail(registerRequest.mail())) {
            throw new EmailAlreadyExistsException(registerRequest.mail());
        }
        User userModel = userMapper.toEntity(registerRequest);
        userModel.setEncodedPassword(passwordEncoder.encode(registerRequest.password()));
        userRepository.save(userModel);
        return userMapper.toResponse(userModel);
    }

    @Override
    public UserLoginResponseDTO login(@Valid UserLoginDTO user) {
        String mail = user.mail();
        User byEmail = userRepository.findByMail(mail).orElseThrow(() -> new BadCredentialsException("invalid.credentials"));

        if (loginAttemptService.IsAccountLocked(mail) || !byEmail.isEnabled()) {
            throw new UserBlockedException(mail);
        }

        if (!passwordEncoder.matches(user.password(), byEmail.getEncodedPassword())) {
            loginAttemptService.recordFailedAttempt(byEmail);
            throw new BadCredentialsException("invalid.credentials");
        }

        UUID sessionId = UUID.randomUUID();
        loginAttemptService.resetAttempts(mail);
        String token = authService.generateRefreshToken(user.mail(), sessionId);
        return new UserLoginResponseDTO(mail, jwtService.generateToken(mail, byEmail.getRoles(), sessionId), token);

    }

    @Override
    @Transactional
    public void logout(String mail, String accessToken) {
        String sessionId = jwtService.extractSessionId(accessToken);
        Integer updatedCount = refreshTokenRepository.revokeCurrentSession(mail, UUID.fromString(sessionId));
        if (updatedCount > 0) {
            redisTokenService.disableAccessToken(accessToken);
        }

    }

    @Override
    @Transactional
    public void logoutAllDevices(String name) {
        Integer logoutCount = authService.revokeAllForUser(name);
        if (logoutCount > 0) {
            redisTokenService.disableAllAccessTokensForUser(name);
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO, String mail, String accessToken) {
        User byEmail = userRepository.findByMail(mail).orElseThrow(() -> new BadCredentialsException("invalid.credentials"));
        String sessionId = jwtService.extractSessionId(accessToken);
        if (!passwordEncoder.matches(changePasswordDTO.oldPassword(), byEmail.getEncodedPassword())) {
            applicationEventPublisher.publishEvent(SecurityEventFactory.create(mail, SecurityEventType.PASSWORD_CHANGED, null, null, null));
            throw new BadCredentialsException("invalid.credentials");
        }
        refreshTokenRepository.revokeAllExceptCurrent(mail, UUID.fromString(sessionId));
        byEmail.setEncodedPassword(passwordEncoder.encode(changePasswordDTO.newPassword()));
        applicationEventPublisher.publishEvent(SecurityEventFactory.create(mail, SecurityEventType.PASSWORD_CHANGED, null, null, null));
        userRepository.save(byEmail);
    }

    @Override
    public void resetPasswordRequest(String name) {
        //TODO: send email with uuid

    }

    @Override
    public void resetPassword(String name) {

    }

    @Override
    @Transactional
    public void closeAccount(String mail, String password) {
        User byEmail = userRepository.findByMail(mail).orElseThrow(() -> new BadCredentialsException("invalid.credentials"));
        if (!passwordEncoder.matches(password, byEmail.getEncodedPassword())) {
            throw new BadCredentialsException("bad.credentials");
        }
        if (userRepository.updateUserEnabled(byEmail.getId()) == 1) {
            byEmail.setEnabled(false);
            authService.revokeAllForUser(mail);
            redisTokenService.disableAllAccessTokensForUser(mail);
            userRepository.save(byEmail);
        }
        //TODO: send email with kafka to retrieve account.

    }


}
