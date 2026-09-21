package com.handegunaydin.habit_tracker.service;


import com.handegunaydin.habit_tracker.entity.RefreshToken;

public interface TokenChainRevocationService {
    void detectRefreshTokenChain(RefreshToken oldRefreshToken);
}
