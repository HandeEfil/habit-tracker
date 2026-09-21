package com.handegunaydin.habit_tracker.habit_tracker.controller;

import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.repository.UserRepository;
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
public class UserControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser(  username = "test@test.com",roles = "CUSTOMER" )
    void getCurrentUser_shouldReturn200_whenAnyAuthenticatedRole() throws Exception {

        User user = new User();
        user.setMail("test@test.com");
        userRepository.save(user);
        mockMvc.perform(get("/api/users/test@test.com")
        ).andExpect(status().isOk());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCurrentUser_shouldReturn403_whenAdminRole() throws Exception {

        mockMvc.perform(get("/api/users/test@test.com")
        ).andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserDetails_shouldReturn403_whenCustomerAccessesAnotherUsersProfile() throws Exception {

        mockMvc.perform(get("/api/users/test@test.com")
        ).andExpect(status().isForbidden());

    }

}


