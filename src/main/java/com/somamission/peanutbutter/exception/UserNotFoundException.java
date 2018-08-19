package com.somamission.peanutbutter.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String reason) {
        super("Cannot get user, reason: " + reason);
    }
}
