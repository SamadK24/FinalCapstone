package com.aurionpro.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.entity.PasswordResetToken;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.PasswordResetTokenRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.PasswordResetEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        System.out.println("🔍 Looking up user with email: " + email);
        
        User user = userRepository.findByUsernameOrEmail(email, email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        System.out.println("✅ User found: " + user.getUsername());
        
        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        
        System.out.println("💾 Reset token saved to database");
        System.out.println("📧 Attempting to send email to: " + user.getEmail());

        // This will throw exception if it fails
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
        
        System.out.println("✅ Email sent successfully!");
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!Objects.equals(newPassword, confirmPassword)) {
            throw new BusinessRuleException("Passwords do not match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new BusinessRuleException("Reset token has expired or already been used");
        }

        User user = resetToken.getUser();

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessRuleException("New password cannot be the same as old password");
        }

        validatePasswordStrength(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        tokenRepository.deleteByUserId(user.getId());

        emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getUsername());
        
        System.out.println("✅ Password reset successful for user: " + user.getUsername());
    }

    public void validateResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("Invalid reset token"));

        if (!resetToken.isValid()) {
            throw new BusinessRuleException("Reset token has expired or already been used");
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessRuleException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new BusinessRuleException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new BusinessRuleException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new BusinessRuleException("Password must contain at least one number");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new BusinessRuleException("Password must contain at least one special character");
        }
    }
}
