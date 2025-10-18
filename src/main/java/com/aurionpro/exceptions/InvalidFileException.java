package com.aurionpro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an uploaded file is invalid (wrong type, too large, or empty).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Optional: Spring will automatically return 400
public class InvalidFileException extends RuntimeException {

    public InvalidFileException() {
        super();
    }

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFileException(Throwable cause) {
        super(cause);
    }
}
