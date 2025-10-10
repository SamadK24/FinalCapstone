package com.aurionpro.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidCaptchaException extends AuthenticationException {

    public InvalidCaptchaException(String msg) {
        super(msg);
    }
}
