package com.handegunaydin.habit_tracker.auth.exception;

public class UserBlockedException extends RuntimeException{
    private final String userBlockedMsg ;
    private final Object[] args;

    public UserBlockedException(String email) {
        this.userBlockedMsg = "user.blocked";
        this.args = new Object[]{email};
    }
}
