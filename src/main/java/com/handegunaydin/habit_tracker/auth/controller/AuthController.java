package com.handegunaydin.habit_tracker.auth.controller;

import com.handegunaydin.habit_tracker.auth.dto.*;
import com.handegunaydin.habit_tracker.auth.service.AuthService;
import com.handegunaydin.habit_tracker.auth.service.LoginRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController{
    private final LoginRegisterService loginRegisterService;
    private final AuthService authService;

    public AuthController(LoginRegisterService loginRegisterService, AuthService authService) {
        this.loginRegisterService = loginRegisterService;
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account."
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid registration data")
    @PostMapping( "/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(loginRegisterService.register(registerRequest));

    }

    @PostMapping( "/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginDTO user){
        return ResponseEntity.ok(loginRegisterService.login(user));
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<TokenPairResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO){
        return ResponseEntity.ok(authService.refresh(requestDTO.refreshToken()));
    }

}
