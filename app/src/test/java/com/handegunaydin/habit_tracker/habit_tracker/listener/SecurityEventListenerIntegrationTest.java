package com.handegunaydin.habit_tracker.habit_tracker.listener;

import com.handegunaydin.habit_tracker.dto.SecurityEvent;
import com.handegunaydin.habit_tracker.enums.SecurityEventType;
import com.handegunaydin.habit_tracker.listener.SecurityEventListener;
import com.handegunaydin.habit_tracker.repository.SecurityEventRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class SecurityEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private SecurityEventListener securityEventListener;

    @Container
    @ServiceConnection
    static MongoDBContainer mongo =
            new MongoDBContainer("mongo:7");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
    @Autowired
    private SecurityEventRepository securityEventRepository;


    @Test
    void handle_ShouldPersistEventLogInNewTransaction_WhenSecurityEventIsHandled() {

        SecurityEvent event = new SecurityEvent("test@test.com",
                SecurityEventType.ACCOUNT_LOCKED,
                null, null, null, null, null);
        eventPublisher.publishEvent(event);
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                assertEquals(1, securityEventRepository.findAll().size()));
    }

    @Test
    void handle_ShouldPersistAuditEvent_WhenPublishingTransactionRollsBack() {

        //TODO: write this test to ensure requires_new part. But first test change password !!!!
    }
}
