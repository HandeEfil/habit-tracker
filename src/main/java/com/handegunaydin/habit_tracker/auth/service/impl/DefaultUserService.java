package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.dto.UserLoginDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserLoginResponseDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterResponseDTO;
import com.handegunaydin.habit_tracker.auth.exception.EmailAlreadyExistsException;
import com.handegunaydin.habit_tracker.auth.exception.UserBlockedException;
import com.handegunaydin.habit_tracker.auth.jwt.JwtService;
import com.handegunaydin.habit_tracker.auth.mapper.UserMapper;
import com.handegunaydin.habit_tracker.auth.service.UserService;
import com.handegunaydin.habit_tracker.user.entity.User;
import com.handegunaydin.habit_tracker.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultUserService implements UserService {


    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


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

        if (!byEmail.isEnabled()) {
            throw new UserBlockedException(mail);
        }

        if (passwordEncoder.matches(user.password(), byEmail.getEncodedPassword())) {
            return new UserLoginResponseDTO(mail, jwtService.generateToken(mail));
        }
        throw new BadCredentialsException("invalid.credentials");
    }
}
