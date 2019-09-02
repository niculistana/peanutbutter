package com.somamission.peanutbutter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String reason) {
        super("Cannot get user, reason: " + reason);
    }
}
