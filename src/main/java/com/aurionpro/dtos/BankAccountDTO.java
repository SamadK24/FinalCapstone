package com.aurionpro.dtos;

import java.math.BigDecimal;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankAccountDTO {
    private Long id;
    private String accountHolderName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String kycStatus;  // VERIFIED, PENDING, REJECTED
    private BigDecimal balance;
    private Boolean verified;
}
