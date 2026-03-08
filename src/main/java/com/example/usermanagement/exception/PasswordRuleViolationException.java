package com.example.usermanagement.exception;

public class PasswordRuleViolationException extends RuntimeException {

    public PasswordRuleViolationException(String message) {
        super(message);
    }
}
