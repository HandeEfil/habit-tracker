package com.handegunaydin.habit_tracker.auth.controller;

import com.handegunaydin.habit_tracker.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092"
})
@ActiveProfiles("test")
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

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void testDataSource() throws SQLException {
        System.out.println("DATASOURCE URL = " +
                dataSource.getConnection().getMetaData().getURL());
    }

    @Test
    void shouldReturn400_whenEmailIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "name": "Test",
                  "password": "Test123!",
                  "mail": "not-an-email",
                  "mobileNumber": "5555555555",
                  "birthDate": "2000-01-01"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());


    }

    @Test
    void shouldReturn201_whenRegisterIsSuccessful() throws Exception {
        String validJSON = """
                  { "name": "Test",
                "password": "Test123!",
                "mail": "test1@test.com",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJSON))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldReturn409_whenEmailAlreadyExists() throws Exception {
        String validJSON = """
                  { "name": "Test",
                "password": "Test123!",
                "mail": "test@test.com",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJSON));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJSON))
                .andExpect(status().isConflict());

    }

    @Test
    void shouldReturn200_whenLoginIsSuccessful() throws Exception {
        String registerJSON = """
                  { "name": "Test",
                "password": "Test123!",
                "mail": "test@test.com",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJSON));
        String loginJSON = """
                  { 
                "password": "Test123!",
                "mail": "test@test.com"
                  }""";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401_whenLoginWithWrongPassword() throws Exception {
        String registerJSON = """
                  { "name": "Test",
                "password": "Test123!",
                "mail": "test@test.com",
                "mobileNumber": "5555555555",
                "birthDate": "2000-01-01"
                  }""";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJSON));
        String loginJSON = """
                  { 
                "password": "Test1234!",
                "mail": "test@test.com"
                  }""";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void shouldReturn401_whenLoginWithNonExistentUser() throws Exception {

        String loginJSON = """
                  { 
                "password": "Test1234!",
                "mail": "test123@test123.com"
                  }""";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJSON))
                .andExpect(status().isUnauthorized());

    }
}
