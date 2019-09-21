package com.somamission.peanutbutter.constants;

public class ErrorMessageConstants {
    private ErrorMessageConstants() {
        throw new IllegalStateException("Constant class");
    }

    private static final String FORMAT_PREFIX = "must be the following format:";
    public static final String EMAIL_FORMAT_REQUIREMENTS = FORMAT_PREFIX + "\n- email must be be unique" +
            "\n- must follow this example format: name@domain.com";
    public static final String USERNAME_FORMAT_REQUIREMENTS = FORMAT_PREFIX + "\n- username be unique,\n- have at least 3 characters" +
            "\n- is not abusive";
    public static final String PASSWORD_FORMAT_REQUIREMENTS = FORMAT_PREFIX + "\n- password must have at least 8 characters," +
            "\n- a mix of numbers, capital and lower case letters";
    public static final String REQUIRED_PARAMETER_NOT_FOUND = "required parameter not found for the request";
}
