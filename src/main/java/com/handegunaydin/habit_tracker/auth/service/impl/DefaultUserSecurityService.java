package com.handegunaydin.habit_tracker.auth.service.impl;

import com.handegunaydin.habit_tracker.auth.service.UserSecurityService;
import org.springframework.stereotype.Service;

@Service
public class DefaultUserSecurityService implements UserSecurityService {
    @Override
    public boolean isOwner(String id, String username) {
        return id.equals(username);
    }
}
