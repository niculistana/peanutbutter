package com.somamission.peanutbutter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends Exception {
    public BadRequestException(String reason) {
        super("Invalid request, reason: " + reason);
    }
}
