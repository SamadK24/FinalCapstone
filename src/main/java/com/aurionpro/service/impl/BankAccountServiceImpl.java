package com.aurionpro.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.BankAccountRepository;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.service.BankAccountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;

    // IFSC format: 4 letters + 0 + 6 alphanumeric
    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    // Account number: 9-18 digits
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{9,18}$");

    @Override
    @Transactional
    public BankAccount addOrUpdateEmployeeBankAccount(Long employeeId, BankAccount bankAccount) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        // Validate employee is active
        if (employee.getStatus() != Employee.Status.ACTIVE) {
            throw new IllegalStateException("Employee account is not active");
        }
        
        // Validate employee belongs to an approved organization
        if (employee.getOrganization() == null) {
            throw new IllegalStateException("Employee is not associated with any organization");
        }
        
        if (employee.getOrganization().getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Employee's organization is not approved yet");
        }

        // Validate bank account details
        validateBankAccountDetails(bankAccount);
        
        // Check for duplicate account
        if (bankAccountRepository.existsDuplicateAccount(
                bankAccount.getAccountNumber(), 
                bankAccount.getIfscCode(), 
                employeeId, 
                null)) {
            throw new IllegalArgumentException("This bank account already exists in the system");
        }

        bankAccount.setEmployee(employee);
        bankAccount.setOrganization(null); // Employee account
        bankAccount.setVerified(false);
        bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.PENDING);
        bankAccount.setPrimary(false); // Organization admin will set primary later

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public BankAccount addOrUpdateOrganizationBankAccount(Long orgId, BankAccount bankAccount, String username) {
        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        // Verify requesting user is the organization admin
        if (!organization.getAdminUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized: Only organization admin can add organization bank accounts");
        }

        // Validate organization is approved
        if (organization.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization must be approved before adding bank accounts");
        }

        // Validate bank account details
        validateBankAccountDetails(bankAccount);
        
        // Check for duplicate account
        if (bankAccountRepository.existsDuplicateAccount(
                bankAccount.getAccountNumber(), 
                bankAccount.getIfscCode(), 
                null, 
                orgId)) {
            throw new IllegalArgumentException("This bank account already exists for this organization");
        }

        bankAccount.setOrganization(organization);
        bankAccount.setEmployee(null); // Organization account
        bankAccount.setVerified(false);
        bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.PENDING);
        bankAccount.setPrimary(true); // Organization accounts can be primary by default

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void approveOrRejectOrganizationKyc(Long bankAccountId, boolean approve, String rejectionReason) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with ID: " + bankAccountId));

        // Verify this is an organization account
        if (bankAccount.getOrganization() == null) {
            throw new IllegalArgumentException("This is not an organization bank account");
        }

        if (approve) {
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.VERIFIED);
            bankAccount.setVerified(true);
            bankAccount.setRejectionReason(null);
        } else {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting KYC");
            }
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.REJECTED);
            bankAccount.setVerified(false);
            bankAccount.setRejectionReason(rejectionReason);
        }

        bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void approveOrRejectEmployeeKyc(Long orgId, Long bankAccountId, boolean approve, String rejectionReason, String username) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with ID: " + bankAccountId));

        // Verify this is an employee account
        if (bankAccount.getEmployee() == null) {
            throw new IllegalArgumentException("This is not an employee bank account");
        }
        
        // Verify employee belongs to the specified organization
        if (!bankAccount.getEmployee().getOrganization().getId().equals(orgId)) {
            throw new SecurityException("This employee does not belong to the specified organization");
        }

        Organization organization = bankAccount.getEmployee().getOrganization();
        
        // Verify requesting user is the organization admin
        if (!organization.getAdminUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized: Only organization admin can approve employee KYC");
        }

        if (approve) {
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.VERIFIED);
            bankAccount.setVerified(true);
            bankAccount.setRejectionReason(null);
        } else {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting KYC");
            }
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.REJECTED);
            bankAccount.setVerified(false);
            bankAccount.setRejectionReason(rejectionReason);
        }

        bankAccountRepository.save(bankAccount);
    }

    @Override
    public List<BankAccount> getBankAccountsForOrganization(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ResourceNotFoundException("Organization not found with ID: " + organizationId);
        }
        return bankAccountRepository.findByOrganizationId(organizationId);
    }
    
    @Override
    public List<BankAccount> getBankAccountsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        return bankAccountRepository.findByEmployee(employee);
    }

    @Override
    public String getStatusByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        return getStatusByEmployee(employee);
    }

    @Override
    public String getStatusByEmployee(Employee employee) {
        // Use createdAt timestamp instead of ID for ordering
        List<BankAccount> accounts = bankAccountRepository.findByEmployeeOrderByCreatedAtDesc(employee);
        
        if (accounts.isEmpty()) 
            return null;

        BankAccount latestAccount = accounts.get(0); // First element is the latest

        return (latestAccount.getKycStatus() != null)
                ? latestAccount.getKycStatus().name()
                : null;
    }
    
    // Private validation method
    private void validateBankAccountDetails(BankAccount bankAccount) {
        // Validate IFSC code format
        if (bankAccount.getIfscCode() == null || !IFSC_PATTERN.matcher(bankAccount.getIfscCode().toUpperCase()).matches()) {
            throw new IllegalArgumentException("Invalid IFSC code format. Must be 4 letters + 0 + 6 alphanumeric characters (e.g., SBIN0001234)");
        }
        
        // Normalize IFSC to uppercase
        bankAccount.setIfscCode(bankAccount.getIfscCode().toUpperCase());
        
        // Validate account number format
        if (bankAccount.getAccountNumber() == null || !ACCOUNT_NUMBER_PATTERN.matcher(bankAccount.getAccountNumber()).matches()) {
            throw new IllegalArgumentException("Invalid account number. Must be 9-18 digits");
        }
        
        // Validate account holder name
        if (bankAccount.getAccountHolderName() == null || bankAccount.getAccountHolderName().trim().length() < 3) {
            throw new IllegalArgumentException("Account holder name must be at least 3 characters");
        }
        
        // Validate bank name
        if (bankAccount.getBankName() == null || bankAccount.getBankName().trim().length() < 3) {
            throw new IllegalArgumentException("Bank name must be at least 3 characters");
        }
    }
}
