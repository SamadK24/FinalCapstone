package com.aurionpro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a request contains invalid input (e.g., blank fields, invalid values).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Optional: Spring returns 400 automatically
public class InvalidInputException extends RuntimeException {

    public InvalidInputException() {
        super();
    }

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputException(Throwable cause) {
        super(cause);
    }
}
