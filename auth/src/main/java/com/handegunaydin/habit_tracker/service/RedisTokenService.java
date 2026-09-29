package com.handegunaydin.habit_tracker.service;

import java.util.Date;

public interface RedisTokenService {
    void disableAccessToken(String accessToken);

    boolean isAccessTokenDisabled(String accessToken);

    void disableAllAccessTokensForUser(String mail);

    boolean isAllAccessTokensForUserDisabled(String mail, Date issuedTime);
}
