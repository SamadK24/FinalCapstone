package com.aurionpro.service;

import org.springframework.stereotype.Component;

import com.aurionpro.exceptions.BusinessRuleException;

@Component
public class PasswordPolicy {

    // At least 1 lower, 1 upper, 1 digit, 1 special, length 8 to 64
    private static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$";

    public void validateNewPassword(String current, String proposed) {
        if (proposed == null || !proposed.matches(REGEX)) {
            throw new BusinessRuleException(
                "Password must be 8-64 chars with upper, lower, digit, and special character");
        }
        if (current != null && current.equals(proposed)) {
            throw new BusinessRuleException("New password cannot be same as current password");
        }
    }
}

