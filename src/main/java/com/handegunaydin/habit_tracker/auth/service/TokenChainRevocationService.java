package com.handegunaydin.habit_tracker.auth.service;

import com.handegunaydin.habit_tracker.auth.entity.RefreshToken;

public interface TokenChainRevocationService {
    void detectRefreshTokenChain(RefreshToken oldRefreshToken);
}
