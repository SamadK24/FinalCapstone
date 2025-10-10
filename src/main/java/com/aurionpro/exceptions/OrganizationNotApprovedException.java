package com.aurionpro.exceptions;

public class OrganizationNotApprovedException extends RuntimeException {
    public OrganizationNotApprovedException(String message) {
        super(message);
    }
}
