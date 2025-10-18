package com.aurionpro.dtos;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfileDTO {
    // Basic Info
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String fullName;
    private String email;
    private String employeeCode;
    private String designation;
    private String department;
    private String dateOfJoining;  // ✅ String type
    
    // Salary Info
    private BigDecimal currentSalary;
    private Long salaryTemplateId;
    private String salaryTemplateName;
    private SalaryTemplateDTO salaryTemplate;  // ✅ Full template details
    
    // KYC Document Status
    private String kycDocumentStatus;  // ✅ ADD THIS
    
    // Bank Account Info
    private String bankKycStatus;      // ✅ ADD THIS
    private String bankAccountNumber;   // ✅ ADD THIS
    private String bankName;           // ✅ ADD THIS
    private List<BankAccountDTO> bankAccounts;  // ✅ Array of bank accounts
    
    // Employee Status
    private String status;  // ✅ ACTIVE, INACTIVE, etc.
}
