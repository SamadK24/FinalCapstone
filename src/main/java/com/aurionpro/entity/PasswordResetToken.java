package com.aurionpro.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    // ✅ FIXED METHOD - With debug logging
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        
        // Debug output
        System.out.println("====== TOKEN VALIDATION ======");
        System.out.println("Token: " + token);
        System.out.println("Current Time: " + now);
        System.out.println("Expiry Time:  " + expiryDate);
        System.out.println("Used: " + used);
        
        // ✅ CORRECT LOGIC: Check if current time is BEFORE expiry
        boolean notUsed = !used;
        boolean notExpired = now.isBefore(expiryDate);
        
        System.out.println("Not Used: " + notUsed);
        System.out.println("Not Expired: " + notExpired);
        System.out.println("Valid: " + (notUsed && notExpired));
        System.out.println("=============================");
        
        return notUsed && notExpired;
    }
}
