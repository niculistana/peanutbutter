package com.somamission.peanutbutter.exception;

public class BadRequestException extends Exception {
    public BadRequestException(String reason) {
        super("Invalid request, reason: " + reason);
    }
}
