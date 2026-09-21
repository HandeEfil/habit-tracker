package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.service.LoginAttemptService;
import com.handegunaydin.habit_tracker.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultLoginAttemptService implements LoginAttemptService {
    private final StringRedisTemplate stringRedisTemplate;

    private final static String ATTEMPT_COUNT_KEY = "login-attempt";
    private final static String ACCOUNT_LOCKED_KEY = "lock-account";

    @Value("${habit_tracker.max_failed_login_attempt.count}")
    private Integer maxAttempt;

    @Value("${habit_tracker.account.lock.duration}")
    private Integer lockDuration;


    @Override
    public long recordFailedAttempt(User user) {
        try {
            if(this.IsAccountLocked(user.getMail())){
                return 0L;
            }
            Long attempt = stringRedisTemplate.opsForValue().increment(ATTEMPT_COUNT_KEY + ":" + user.getMail());
            if (attempt == null) {
                return 0L;
            }
            if (attempt.equals(1L)) {
                stringRedisTemplate.expire(ATTEMPT_COUNT_KEY + ":" + user.getMail(), Duration.ofMinutes(15));
            }
            if (maxAttempt <= attempt) {
                // TODO: Trigger kafka event to inform user
                registerLockForAccount(user.getMail());
            }
            return attempt;
        } catch (RedisConnectionFailureException redisConnectionFailureException) {
            return 0L;

        }

    }

    @Override
    public void resetAttempts(String email) {
        try {
            stringRedisTemplate.delete(ATTEMPT_COUNT_KEY + ":" + email);
        } catch (RedisConnectionFailureException redisConnectionFailureException) {

        }
    }

    @Override
    public void resetLock(String email) {
        try {
            stringRedisTemplate.delete(ACCOUNT_LOCKED_KEY + ":" + email);
        } catch (RedisConnectionFailureException redisConnectionFailureException) {

        }
    }

    @Override
    public void registerLockForAccount(String email) {
        try {
            stringRedisTemplate.opsForValue().set(ACCOUNT_LOCKED_KEY + ":" + email, "locked", Duration.ofMinutes(lockDuration));
            this.resetAttempts(email);

        } catch (RedisConnectionFailureException redisConnectionFailureException) {

        }

    }

    @Override
    public boolean IsAccountLocked(String email) {
        try {
            return stringRedisTemplate.opsForValue().get(ACCOUNT_LOCKED_KEY + ":" + email) != null;
        } catch (RedisConnectionFailureException redisConnectionFailureException) {
            return false;
        }
    }
}
