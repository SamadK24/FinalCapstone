package com.aurionpro.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested organization does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Optional: Spring will automatically return 404
public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException() {
        super();
    }

    public OrganizationNotFoundException(String message) {
        super(message);
    }

    public OrganizationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrganizationNotFoundException(Throwable cause) {
        super(cause);
    }
}

