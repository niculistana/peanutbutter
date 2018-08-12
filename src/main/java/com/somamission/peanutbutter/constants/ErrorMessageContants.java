package com.somamission.peanutbutter.constants;

public class ErrorMessageContants {
    public static final String EMAIL_FORMAT_REQUIREMENTS = "email must be be unique,\n and come from a supported host";
    public static final String USERNAME_FORMAT_REQUIREMENTS = "username be unique,\n and must have at least 3 characters";
    public static final String PASSWORD_FORMAT_REQUIREMENTS = "password must have at least 8 characters,\n" +
            "a mix of numbers, capital and lower case letters,\n a special character\n" +
            "and has not been recently used";
    public static String EMPTY_PARAMETER_MESSAGE = "no parameters supplied for the request";
    public static String REQUIRED_PARAMETER_NOT_FOUND = "required parameter not found for the request";
}
