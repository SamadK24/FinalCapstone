package com.aurionpro.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.aurionpro.service.PasswordResetEmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailServiceImpl implements PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@auropay.com");
        message.setTo(toEmail);
        message.setSubject("🔒 Password Reset Request - AuroPay Payroll");
        message.setText(buildPasswordResetEmail(userName, resetLink));
        
        try {
            mailSender.send(message);
            log.info("✅ Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

  

    private String buildPasswordResetEmail(String userName, String resetLink) {
        return String.format("""
            Dear %s,
            
            We received a request to reset your password for your AuroPay Payroll account.
            
            Click the link below to reset your password:
            %s
            
            ⏰ This link will expire in 30 minutes for security reasons.
            
            🔒 Security Tips:
            • Never share this link with anyone
            • Our team will never ask for your password
            • If you didn't request this, please ignore this email
            
            Best regards,
            AuroPay Security Team
            """, userName, resetLink);
    }

    private String buildPasswordResetSuccessEmail(String userName) {
        return String.format("""
            Dear %s,
            
            ✓ Your password has been successfully reset.
            
            You can now log in to your AuroPay Payroll account.
            
            Login at: http://localhost:4200/login
            
            🔒 If you did not perform this action, contact us immediately.
            
            Best regards,
            AuroPay Security Team
            """, userName);
    }



	@Override
	public void sendPasswordResetSuccessEmail(String toEmail, String userName) {
		// TODO Auto-generated method stub
		
	}
}
