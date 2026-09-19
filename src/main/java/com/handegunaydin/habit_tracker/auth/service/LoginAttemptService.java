package com.handegunaydin.habit_tracker.auth.service;

import com.handegunaydin.habit_tracker.user.entity.User;

public interface LoginAttemptService {
    long recordFailedAttempt(User user);
    void resetAttempts(String email);
    void resetLock(String email);
    void registerLockForAccount(String email);
    boolean IsAccountLocked(String email);
}
