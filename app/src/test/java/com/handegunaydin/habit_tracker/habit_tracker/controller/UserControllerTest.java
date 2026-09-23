package com.handegunaydin.habit_tracker.habit_tracker.controller;

import com.handegunaydin.habit_tracker.entity.User;
import com.handegunaydin.habit_tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    @WithMockUser(username = "test@test.com", roles = "CUSTOMER")
    void getCurrentUser_shouldReturn200_whenAnyAuthenticatedRole() throws Exception {
        User user = new User();
        user.setMail("test@test.com");
        userRepository.save(user);
        mockMvc.perform(get("/api/users/test@test.com")
        ).andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "ADMIN")
    void getCurrentUser_shouldReturn403_whenAdminRole() throws Exception {

        mockMvc.perform(get("/api/users/test@test.com")
        ).andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "CUSTOMER")
    void getUserDetails_shouldReturn403_whenCustomerAccessesAnotherUsersProfile() throws Exception {

        mockMvc.perform(get("/api/users/test1@test.com")
        ).andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "CUSTOMER")
    void updateUser_whenOwner_returns200() throws Exception {
        User user = new User();
        user.setMail("test1234@test.com");
        userRepository.save(user);
        String requestBody = """
                { "name": "Test",
                "mail": "test@test.com",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""";
        mockMvc.perform(put("/api/users/update").contentType(MediaType.APPLICATION_JSON).content(requestBody)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "CUSTOMER")
    void updateUser_whenNotOwner_returns403() throws Exception {
        User user = new User();
        user.setMail("test1@test.com");
        userRepository.save(user);
        String requestBody = """
                { "name": "Test",
                "mail": "test1@test.com",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""";
        mockMvc.perform(put("/api/users/update").contentType(MediaType.APPLICATION_JSON).content(requestBody)
        ).andExpect(status().isForbidden());

    }

}


