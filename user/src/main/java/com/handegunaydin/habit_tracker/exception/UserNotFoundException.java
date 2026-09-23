package com.handegunaydin.habit_tracker.exception;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException{
    private final String msg ;
    private final Object[] args;


    public UserNotFoundException(String msg, Object[] args) {
        this.msg = msg;
        this.args = args;
    }
}
