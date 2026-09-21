package com.handegunaydin.habit_tracker.exception;

import lombok.Getter;

@Getter
public class UserBlockedException extends RuntimeException{
    private final String userBlockedMsg ;
    private final Object[] args;

    public UserBlockedException(String email) {
        this.userBlockedMsg = "user.blocked";
        this.args = new Object[]{email};
    }
}
