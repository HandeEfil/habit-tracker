package com.handegunaydin.habit_tracker.auth.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException{
    private final String mailDuplicateMsg ;
    private final Object[] args;

    public EmailAlreadyExistsException(String email) {
        this.mailDuplicateMsg = "user.email.already.exists";
        this.args = new Object[]{email};
    }
}
