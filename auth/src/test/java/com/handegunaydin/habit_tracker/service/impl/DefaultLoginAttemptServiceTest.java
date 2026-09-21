package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.entity.User;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class DefaultLoginAttemptServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Value("${habit_tracker.max_failed_login_attempt.count}")
    private int maxFailedLoginAttempts;

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }


    @BeforeEach
    void setUp() {
        loginAttemptService.resetAttempts(testUser().getMail());
        loginAttemptService.resetLock(testUser().getMail());
    }

    @Autowired
    private DefaultLoginAttemptService loginAttemptService;

    private User testUser() {
        User u = new User();
        u.setMail("test@test.com");
        return u;
    }

    private User testUser123() {
        User u = new User();
        u.setMail("test123@test123.com");
        return u;
    }

    @Test
    void firstFailedAttempt_doesNotLockAccount() {
        User user = testUser();
        loginAttemptService.recordFailedAttempt(user);
        assertFalse(loginAttemptService.IsAccountLocked(user.getMail()));
    }

    @Test
    void reachingMaxAttempts_locksAccount() {

        User user = testUser();
        for (int i = 0; i < maxFailedLoginAttempts; i++) {
            loginAttemptService.recordFailedAttempt(user);
        }
        assertTrue(loginAttemptService.IsAccountLocked(user.getMail()));
    }

    @Test
    void resetAttempts_clearsCounterButNotExistingLock() {

        User user = testUser();
        for (int i = 0; i < maxFailedLoginAttempts; i++) {
            loginAttemptService.recordFailedAttempt(user);
        }
        assertTrue(loginAttemptService.IsAccountLocked(user.getMail()));
        loginAttemptService.resetAttempts(user.getMail());
        assertTrue(loginAttemptService.IsAccountLocked(user.getMail()));
        assertEquals(0, loginAttemptService.recordFailedAttempt(user));
        loginAttemptService.resetLock(user.getMail());
        assertEquals(1, loginAttemptService.recordFailedAttempt(user));
    }


    @Test
    void differentUsers_haveIndependentCounters() {

        User user = testUser();
        Long attemptCountUser = 0L;
        Long attemptCountUser123 = 0L;
        for (int i = 0; i < maxFailedLoginAttempts - 1; i++) {
            attemptCountUser = loginAttemptService.recordFailedAttempt(user);
        }
        User user123 = testUser123();
        for (int i = 0; i < maxFailedLoginAttempts - 2; i++) {
            attemptCountUser123 = loginAttemptService.recordFailedAttempt(user123);
        }
        assertEquals(maxFailedLoginAttempts - 1, attemptCountUser);
        assertEquals(maxFailedLoginAttempts - 2, attemptCountUser123);

    }

}
