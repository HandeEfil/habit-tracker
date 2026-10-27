package com.handegunaydin.habit_tracker.controller;

import com.handegunaydin.habit_tracker.dto.NotificationDTO;
import com.handegunaydin.habit_tracker.entity.Notification;
import com.handegunaydin.habit_tracker.entity.RegisterEventNotification;
import com.handegunaydin.habit_tracker.enums.NotificationType;
import com.handegunaydin.habit_tracker.repository.NotificationRepository;
import com.handegunaydin.habit_tracker.service.NotificationService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.testcontainers.kafka.KafkaContainer;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
public class NotificationControllerTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private MockMvc mockMvc;



    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("apache/kafka:3.8.0")
            );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );
    }
    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:7");
    private final UUID testUID = UUID.randomUUID();
    private final UUID test1UID = UUID.randomUUID();

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @BeforeEach
    public void setUp(){
        notificationRepository.deleteAll();
        notificationRepository.save(getEntity("test@test.com",testUID));
        notificationRepository.save(getEntity("test1@test.com",test1UID));

    }

    @Test
    @WithMockUser(username = "test@test.com",roles = "CUSTOMER")
    void getNotifications_shouldReturn200AndNotificationsForAuthenticatedCustomer() throws Exception {
        MvcResult mvcResult = getNotifications().andExpect(status().isOk()).andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        List<NotificationDTO> notificationDTOList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<NotificationDTO>>(){});
        assertEquals(1, notificationDTOList.size());

    }

    @Test
    @WithMockUser(username = "test123@test.com",roles = "CUSTOMER")
    void getNotifications_shouldReturnEmptyListWhenUserHasNoNotifications() throws Exception {
        MvcResult mvcResult = getNotifications().andExpect(status().isOk()).andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        List<NotificationDTO> notificationDTOList = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<NotificationDTO>>(){});
        assertEquals(0, notificationDTOList.size());
    }

    @Test
    void getNotifications_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        getNotifications().andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test123@test.com",roles = "ADMIN")
    void getNotifications_ShouldReturn403_WhenUserIsNotCustomer() throws Exception {
        getNotifications().andExpect(status().isForbidden());

    }

    @Test
    @WithMockUser(username = "test@test.com",roles = "CUSTOMER")
    void markAsRead_ShouldReturn200_WhenSuccessful() throws Exception {
        readNotification(testUID.toString()).andExpect(status().isOk());
        List<Notification> byRecipient = notificationRepository.findByRecipient("test@test.com");
        assertEquals(1, byRecipient.size());
        assertTrue(byRecipient.get(0).isRead());

    }

    @Test
    @WithMockUser(username = "test@test.com",roles = "CUSTOMER")
    void markAsRead_shouldNotMarkAnotherUsersNotificationAsRead() throws Exception {
        readNotification(test1UID.toString()).andExpect(status().isOk());
        List<Notification> byRecipient = notificationRepository.findByRecipient("test1@test.com");
        assertEquals(1, byRecipient.size());
        assertFalse(byRecipient.getFirst().isRead());
    }

    @Test
    void markAsRead_shouldReturn401WhenUserIsUnauthenticated() throws Exception {
        readNotification(test1UID.toString()).andExpect(status().isUnauthorized());

    }

    @Test
    @WithMockUser(username = "test@test.com",roles = "ADMIN")
    void markAsRead_shouldReturn403WhenUserDoesNotHaveCustomerRole() throws Exception {
        readNotification(test1UID.toString()).andExpect(status().isForbidden());

    }

    private ResultActions getNotifications() throws Exception {

        return mockMvc.perform(get("/notifications")
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions readNotification(String id) throws Exception {

        return mockMvc.perform(patch("/notifications/{id}/read", id)
                .contentType(MediaType.APPLICATION_JSON));
    }



    private static RegisterEventNotification getEntity(String mail, UUID uid) {
        RegisterEventNotification registerEventNotification = new RegisterEventNotification();
        registerEventNotification.setNotificationType(NotificationType.WELCOME);
        registerEventNotification.setRecipient(mail);
        registerEventNotification.setMessage("Hello World");
        registerEventNotification.setPublicId(uid);
        return registerEventNotification;
    }
}
