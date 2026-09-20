package com.handegunaydin.habit_tracker.user.controller;

import com.handegunaydin.habit_tracker.auth.enums.Role;
import com.handegunaydin.habit_tracker.auth.jwt.JwtService;
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
public class AdminControllerTest {


    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Test
    void getAdminUsers_shouldReturn200_whenAdminRole() throws Exception {

        String token = jwtService.generateToken("test@test.com", List.of(Role.ADMIN));
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isOk());

    }

    @Test
    void getAdminUsers_shouldReturn403_whenCustomerRole() throws Exception {

        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token)
        ).andExpect(status().isForbidden());

    }

    @Test
    void getAdminUsers_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
        ).andExpect(status().isUnauthorized());

    }

    @Test
    void getAdminUsers_shouldReturn401_whenTokenMalformed() throws Exception {

        String token = jwtService.generateToken("test@test.com", List.of(Role.CUSTOMER));
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + token + "xx")
        ).andExpect(status().isUnauthorized());

    }

}
