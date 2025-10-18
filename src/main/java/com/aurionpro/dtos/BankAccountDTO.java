package com.aurionpro.dtos;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDTO {

    private Long id;

    @NotBlank(message = "Account holder name is required")
    @Size(min = 3, max = 100, message = "Account holder name must be between 3 and 100 characters")
    private String accountHolderName;

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{9,18}$", message = "Account number must be 9-18 digits")
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    @Size(min = 3, max = 100, message = "Bank name must be between 3 and 100 characters")
    private String bankName;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format (e.g., SBIN0001234)")
    private String ifscCode;

    private String branchName;

    private String kycStatus; // VERIFIED, PENDING, REJECTED
    private BigDecimal balance;

    private boolean verified;
    private boolean isPrimary;
    private String rejectionReason;
    private String createdAt;
}
