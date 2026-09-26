package com.handegunaydin.habit_tracker.service.impl;


import com.handegunaydin.habit_tracker.dto.UserLoginDTO;
import com.handegunaydin.habit_tracker.dto.UserLoginResponseDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.dto.UserRegisterResponseDTO;
import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.exception.EmailAlreadyExistsException;
import com.handegunaydin.habit_tracker.exception.UserBlockedException;
import com.handegunaydin.habit_tracker.jwt.JwtService;
import com.handegunaydin.habit_tracker.mapper.UserMapper;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import com.handegunaydin.habit_tracker.service.AuthService;
import com.handegunaydin.habit_tracker.service.LoginAttemptService;
import com.handegunaydin.habit_tracker.service.LoginRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (passwordEncoder.matches(user.password(), byEmail.getEncodedPassword())) {
            loginAttemptService.resetAttempts(mail);
            String token = authService.generateRefreshToken(user.mail());
            return new UserLoginResponseDTO(mail, jwtService.generateToken(mail, byEmail.getRoles()), token);
        }

        loginAttemptService.recordFailedAttempt(byEmail);
        throw new BadCredentialsException("invalid.credentials");
    }

    @Override
    @Transactional
    public void closeAccount(String mail, String password) {
        User byEmail = userRepository.findByMail(mail).orElseThrow(() -> new BadCredentialsException("invalid.credentials"));
        if (!passwordEncoder.matches(password, byEmail.getEncodedPassword())) {
            throw new BadCredentialsException("bad.credentials");
        }
        if(userRepository.updateUserEnabled(byEmail.getId()) == 1) {
            byEmail.setEnabled(false);
            authService.revokeAllForUser(mail);
            userRepository.save(byEmail);
        }
        //TODO: send email with kafka to retrieve account.


    }
}
