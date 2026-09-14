package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.dto.UserFailedLoginAttemptEvent;
import com.handegunaydin.habit_tracker.user.entity.User;
import com.handegunaydin.habit_tracker.auth.service.LoginAttemptService;
import com.handegunaydin.habit_tracker.notification.enums.EventNotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DefaultLoginAttemptService implements LoginAttemptService {
    private final StringRedisTemplate stringRedisTemplate;

    private final String ATTEMPT_COUNT_KEY = "login-attempt";

    @Value("${habit_tracker.max_failed_login_attempt.count}")
    private Integer MAX_ATTEMPT;

    private final LoginFailedAttemptProducer failedAttemptProducer;

    @Override
    public void recordFailedAttempt(User user) {
        Long attempt = stringRedisTemplate.opsForValue().increment(ATTEMPT_COUNT_KEY);
        if(attempt != null && attempt.equals(1L)){
            stringRedisTemplate.expire(ATTEMPT_COUNT_KEY, Duration.ofMinutes(15));
        }
        if(MAX_ATTEMPT <= this.getAttemptCount(user.getMail())){
//            failedAttemptProducer.publishFailedAttempt(new UserFailedLoginAttemptEvent(user.getName(), user.getMail(), user.getNotificationTypes(), EventNotificationType.FAILED_LOGIN));
            user.setEnabled(false);
        }

    }

    @Override
    public Integer getAttemptCount(String email) {
        return Integer.valueOf(stringRedisTemplate.opsForValue().get(ATTEMPT_COUNT_KEY));

    }

    @Override
    public void resetAttempts(String email) {
        stringRedisTemplate.delete(ATTEMPT_COUNT_KEY);
    }
}
