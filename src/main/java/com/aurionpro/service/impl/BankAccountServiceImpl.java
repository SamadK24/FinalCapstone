package com.aurionpro.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{9,18}$");

    @Override
    @Transactional
    public BankAccount addOrUpdateEmployeeBankAccount(Long employeeId, BankAccount bankAccount) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (employee.getStatus() != Employee.Status.ACTIVE) {
            throw new IllegalStateException("Employee account is not active");
        }

        if (employee.getOrganization() == null || employee.getOrganization().getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Employee's organization is not approved yet");
        }

        validateBankAccountDetails(bankAccount);

        if (bankAccountRepository.existsDuplicateAccount(
                bankAccount.getAccountNumber(),
                bankAccount.getIfscCode(),
                employeeId,
                null)) {
            throw new IllegalArgumentException("This bank account already exists in the system");
        }

        if (bankAccount.getBalance() == null) {
            bankAccount.setBalance(BigDecimal.ZERO);
        }

        bankAccount.setEmployee(employee);
        bankAccount.setOrganization(null);
        bankAccount.setVerified(false);
        bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.PENDING);
        bankAccount.setPrimary(false);

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public BankAccount addOrUpdateOrganizationBankAccount(Long orgId, BankAccount bankAccount, String username) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        if (!organization.getAdminUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized: Only organization admin can add organization bank accounts");
        }

        if (organization.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization must be approved before adding bank accounts");
        }

        validateBankAccountDetails(bankAccount);

        if (bankAccountRepository.existsDuplicateAccount(
                bankAccount.getAccountNumber(),
                bankAccount.getIfscCode(),
                null,
                orgId)) {
            throw new IllegalArgumentException("This bank account already exists for this organization");
        }

        if (bankAccount.getBalance() == null) {
            bankAccount.setBalance(BigDecimal.ZERO);
        }

        bankAccount.setOrganization(organization);
        bankAccount.setEmployee(null);
        bankAccount.setVerified(false);
        bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.PENDING);
        bankAccount.setPrimary(true);

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void approveOrRejectOrganizationKyc(Long bankAccountId, boolean approve, String rejectionReason) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with ID: " + bankAccountId));

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

        if (bankAccount.getEmployee() == null) {
            throw new IllegalArgumentException("This is not an employee bank account");
        }

        if (!bankAccount.getEmployee().getOrganization().getId().equals(orgId)) {
            throw new SecurityException("This employee does not belong to the specified organization");
        }

        Organization organization = bankAccount.getEmployee().getOrganization();

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
        List<BankAccount> accounts = bankAccountRepository.findByEmployeeOrderByCreatedAtDesc(employee);

        if (accounts.isEmpty())
            return null;

        BankAccount latestAccount = accounts.get(0);
        return (latestAccount.getKycStatus() != null) ? latestAccount.getKycStatus().name() : null;
    }

    @Override
    public List<BankAccount> getEmployeeBankAccountsForOrganization(Long orgId) {
        List<Employee> employees = employeeRepository.findByOrganizationId(orgId);

        List<BankAccount> allAccounts = new ArrayList<>();
        for (Employee emp : employees) {
            allAccounts.addAll(bankAccountRepository.findByEmployeeId(emp.getId()));
        }

        return allAccounts;
    }

    // ------------------ Private Helpers ------------------
    private void validateBankAccountDetails(BankAccount bankAccount) {
        if (bankAccount.getIfscCode() == null || !IFSC_PATTERN.matcher(bankAccount.getIfscCode().toUpperCase()).matches()) {
            throw new IllegalArgumentException("Invalid IFSC code format (e.g., SBIN0001234)");
        }
        bankAccount.setIfscCode(bankAccount.getIfscCode().toUpperCase());

        if (bankAccount.getAccountNumber() == null || !ACCOUNT_NUMBER_PATTERN.matcher(bankAccount.getAccountNumber()).matches()) {
            throw new IllegalArgumentException("Invalid account number. Must be 9-18 digits");
        }

        if (bankAccount.getAccountHolderName() == null || bankAccount.getAccountHolderName().trim().length() < 3) {
            throw new IllegalArgumentException("Account holder name must be at least 3 characters");
        }

        if (bankAccount.getBankName() == null || bankAccount.getBankName().trim().length() < 3) {
            throw new IllegalArgumentException("Bank name must be at least 3 characters");
        }
    }
}
