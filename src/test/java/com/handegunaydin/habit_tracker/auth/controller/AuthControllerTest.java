package com.handegunaydin.habit_tracker.auth.controller;

import com.handegunaydin.habit_tracker.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Value("${habit_tracker.max_failed_login_attempt.count}")
    private int maxFailedLoginAttempts;

    @Test
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        registerUser("not-an-email", "Test123!")
                .andExpect(status().isBadRequest());


    }

    @Test
    void shouldReturn201_whenRegisterIsSuccessful() throws Exception {
        registerUser("test1234@test.com", "Test123!");
    }


    @Test
    void shouldReturn409_whenEmailAlreadyExists() throws Exception {
        registerUser("test@test.com", "Test123!");
        registerUser("test@test.com", "Test123!")
                .andExpect(status().isConflict());

    }

    @Test
    void shouldReturn200_whenLoginIsSuccessful() throws Exception {
        registerUser("test@test.com", "Test123!");
        loginUser("test@test.com", "Test123!")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401_whenLoginWithWrongPassword() throws Exception {
        registerUser("test@test.com", "Test123!");
        loginUser("test@test.com", "Test1234!")
                .andExpect(status().isUnauthorized());

    }

    @Test
    void shouldReturn401_whenLoginWithNonExistentUser() throws Exception {

        loginUser("test123@test123.com", "Test1234!")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_afterMaxFailedAttempts_returnsLockedEvenWithCorrectPassword() throws Exception {
        registerUser("blockedUser@block.com", "Test1234!");
        for (int i = 0; i < maxFailedLoginAttempts; i++) {
            loginUser("blockedUser@block.com", "Test12345!");
        }
        loginUser("blockedUser@block.com", "Test1234!").andExpect(status().isTooManyRequests());
    }


    private ResultActions registerUser(String email, String password) throws Exception {
        String validJSON = """
                  { "name": "Test",
                "password": "%s",
                "mail": "%s",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""".formatted(password, email);
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJSON));
    }

    private ResultActions loginUser(String email, String password) throws Exception {
        String validJSON = """
                  { 
                "password": "%s",
                "mail": "%s"
                  }""".formatted(password, email);
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJSON));
    }
}
