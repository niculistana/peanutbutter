package com.somamission.peanutbutter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserFoundException extends Exception {
  public UserFoundException(String reason) {
    super(reason);
  }
}
