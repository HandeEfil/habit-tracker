package com.handegunaydin.habit_tracker.habit_tracker.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAdminUsers_shouldReturn200_whenAdminRole() throws Exception {

        mockMvc.perform(get("/api/admin/users")
        ).andExpect(status().isOk());

    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAdminUsers_shouldReturn403_whenCustomerRole() throws Exception {

        mockMvc.perform(get("/api/admin/users")
        ).andExpect(status().isForbidden());

    }

    @Test
    void getAdminUsers_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
        ).andExpect(status().isUnauthorized());

    }

    @Test
    void getAdminUsers_shouldReturn401_whenTokenMalformed() throws Exception {

        mockMvc.perform(get("/api/admin/users")
        ).andExpect(status().isUnauthorized());

    }

}
