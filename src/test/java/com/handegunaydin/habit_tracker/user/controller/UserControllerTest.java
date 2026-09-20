package com.handegunaydin.habit_tracker.user.controller;


import com.handegunaydin.habit_tracker.auth.enums.Role;
import com.handegunaydin.habit_tracker.auth.jwt.JwtService;
import com.handegunaydin.habit_tracker.user.entity.User;
import com.handegunaydin.habit_tracker.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public class UserControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;
    @Test
    void getCurrentUser_shouldReturn200_whenAnyAuthenticatedRole() throws Exception {

        User user = new User();
        user.setMail("test@test.com");
        userRepository.save(user);
        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        mockMvc.perform(get("/api/users/test@test.com")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk());

    }

    @Test
    void getCurrentUser_shouldReturn403_whenAdminRole() throws Exception {

        String token = jwtService.generateToken("test@test.com", List.of(Role.ADMIN));
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isForbidden());

    }

}


