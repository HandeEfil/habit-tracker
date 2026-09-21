package com.handegunaydin.habit_tracker.service;

public interface UserSecurityService {
    boolean isOwner(String id, String username);
}
