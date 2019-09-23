package com.somamission.peanutbutter.constants;

public class ErrorMessageConstants {
  private ErrorMessageConstants() {
    throw new IllegalStateException("Constant class");
  }

  public static final String EMAIL_FORMAT_REQUIREMENTS =
      "email must follow this example format: name@domain.com";
  public static final String USERNAME_FORMAT_REQUIREMENTS = "username must not be forbidden";
  public static final String PASSWORD_FORMAT_REQUIREMENTS =
      "password must have at least 8 characters, and a mix of numbers, capital and lower case letters";
  public static final String URL_FORMAT_REQUIREMENTS =
      "URL must follow this example format: htt[://example.com";
  public static final String REQUIRED_PARAMETER_NOT_FOUND =
      "required parameter not found for the request";
}
