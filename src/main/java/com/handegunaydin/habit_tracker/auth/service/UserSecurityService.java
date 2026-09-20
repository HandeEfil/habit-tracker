package com.handegunaydin.habit_tracker.auth.service;

public interface UserSecurityService {
    boolean isOwner(String id, String username);
}
