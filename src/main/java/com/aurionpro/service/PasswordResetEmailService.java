package com.aurionpro.service;

public interface PasswordResetEmailService {
    void sendPasswordResetEmail(String toEmail, String userName, String resetToken);
    void sendPasswordResetSuccessEmail(String toEmail, String userName);
}
