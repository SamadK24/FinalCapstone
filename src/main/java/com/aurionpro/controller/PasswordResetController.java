package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ForgotPasswordRequest;
import com.aurionpro.dtos.MessageResponse;
import com.aurionpro.dtos.ResetPasswordRequest;
import com.aurionpro.service.impl.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j  // ✅ ADD THIS
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("📧 Password reset requested for email: {}", request.getEmail());
            passwordResetService.sendPasswordResetEmail(request.getEmail());
            log.info("✅ Password reset email sent successfully to: {}", request.getEmail());
            
            return ResponseEntity.ok(new MessageResponse(
                "If an account exists with this email, you will receive a password reset link shortly."
            ));
        } catch (Exception e) {
            // ✅ LOG THE ERROR - This is critical!
            log.error("❌ Failed to send password reset email to: {}", request.getEmail(), e);
            
            // Still return generic message (for security - don't reveal if user exists)
            return ResponseEntity.ok(new MessageResponse(
                "If an account exists with this email, you will receive a password reset link shortly."
            ));
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<MessageResponse> validateToken(@RequestParam String token) {
        log.info("🔍 Validating reset token: {}", token);
        passwordResetService.validateResetToken(token);
        return ResponseEntity.ok(new MessageResponse("Token is valid"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("🔐 Password reset attempt for token: {}", request.getToken());
        passwordResetService.resetPassword(
            request.getToken(),
            request.getNewPassword(),
            request.getConfirmPassword()
        );
        log.info("✅ Password reset successful");
        return ResponseEntity.ok(new MessageResponse("Password reset successful. Please login with your new password."));
    }
}
