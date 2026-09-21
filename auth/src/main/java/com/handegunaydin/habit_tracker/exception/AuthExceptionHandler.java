package com.handegunaydin.habit_tracker.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.handegunaydin.habit_tracker.")
@RequiredArgsConstructor
public class AuthExceptionHandler {

    private final MessageSource messageSource;


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailAlreadyExistsException ex) {
        String message = messageSource.getMessage(
                ex.getMailDuplicateMsg(), ex.getArgs(), LocaleContextHolder.getLocale()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "email_exists", "message", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_credentials", "message", ex.getMessage()));
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<Map<String, String>> handleUserBlockedException(UserBlockedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "user_blocked", "message", messageSource.getMessage(ex.getUserBlockedMsg(), ex.getArgs(), LocaleContextHolder.getLocale())));
    }
}
