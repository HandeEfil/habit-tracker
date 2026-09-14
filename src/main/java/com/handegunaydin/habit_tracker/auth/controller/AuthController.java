package com.handegunaydin.habit_tracker.auth.controller;

import com.handegunaydin.habit_tracker.auth.dto.UserLoginDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserLoginResponseDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterDTO;
import com.handegunaydin.habit_tracker.auth.dto.UserRegisterResponseDTO;
import com.handegunaydin.habit_tracker.auth.service.UserService;
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
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account."
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid registration data")
    @PostMapping( value = "/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(registerRequest));

    }

    @PostMapping( value = "/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginDTO user){
        return ResponseEntity.ok(userService.login(user));
    }

}
