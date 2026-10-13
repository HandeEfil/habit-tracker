package com.handegunaydin.habit_tracker.service.impl;

import com.handegunaydin.habit_tracker.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class DefaultRedisTokenService implements RedisTokenService {

    private final static String BLACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate stringRedisTemplate;
    @Value("${jwt.expiration}")
    private long accessTokenDuration;

    @Override
    public void disableAccessToken(String accessToken) { //throw if anything goes wrong.
        stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "disabled", Duration.ofMillis(accessTokenDuration));

    }

    @Override
    public boolean isAccessTokenDisabled(String accessToken) {
        try {
            return stringRedisTemplate.opsForValue().get(BLACKLIST_PREFIX + accessToken) != null;
        } catch (RedisConnectionFailureException redisConnectionFailureException) {

        }
        return false;
    }

    @Override
    public void disableAllAccessTokensForUser(String mail) {
        try {
            stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX + mail, String.valueOf(Instant.now()), Duration.ofMillis(accessTokenDuration));

        } catch (RedisConnectionFailureException redisConnectionFailureException) {

        }

    }

    @Override
    public boolean isAllAccessTokensForUserDisabled(String mail, Date issuedTime) {
        try {
            String timeOfDisable = stringRedisTemplate.opsForValue().get(BLACKLIST_PREFIX + mail);
            if (timeOfDisable == null) {
                return false;
            }
            return !issuedTime.toInstant().isAfter(Instant.parse(timeOfDisable));

        } catch (RedisConnectionFailureException redisConnectionFailureException) {
            return false;

        }
    }
}
