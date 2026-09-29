package com.handegunaydin.habit_tracker.controller;

import com.handegunaydin.habit_tracker.dto.*;
import com.handegunaydin.habit_tracker.service.AuthService;
import com.handegunaydin.habit_tracker.service.LoginRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
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
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loginRegisterService.register(registerRequest));

    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginDTO user) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(loginRegisterService.login(user));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Boolean> logout(@RequestHeader(value = "Authorization", required = true) String accessToken, Authentication authentication) {
        loginRegisterService.logout(authentication.getName(), accessToken.substring(7));
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/logout-all-devices")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity logoutAllDevices(Authentication authentication) {
        loginRegisterService.logoutAllDevices(authentication.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO, Authentication authentication, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = authHeader.substring(7);
        loginRegisterService.changePassword(changePasswordDTO, authentication.getName(), accessToken);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity resetPassword(Authentication authentication) {
        loginRegisterService.resetPasswordRequest(authentication.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<TokenPairResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO requestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authService.refresh(requestDTO));
    }

    @PostMapping(value = "/close-account")
    @PreAuthorize(value = "hasRole('CUSTOMER')")
    public ResponseEntity<Void> closeAccount(@Valid @RequestBody CloseAccountDTO closeAccountDTO, Authentication authentication) {
        loginRegisterService.closeAccount(authentication.getName(), closeAccountDTO.password());
        return ResponseEntity
                .ok(null);

    }

}
