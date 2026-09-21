package com.handegunaydin.habit_tracker.service;

import com.handegunaydin.habit_tracker.entity.User;

public interface LoginAttemptService {
    long recordFailedAttempt(User user);
    void resetAttempts(String email);
    void resetLock(String email);
    void registerLockForAccount(String email);
    boolean IsAccountLocked(String email);
}
