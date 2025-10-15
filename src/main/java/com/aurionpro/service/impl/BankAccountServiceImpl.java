package com.aurionpro.service.impl;

import java.util.Comparator;
import java.util.List;

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

    @Override
    @Transactional
    public BankAccount addOrUpdateEmployeeBankAccount(Long employeeId, BankAccount bankAccount) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        bankAccount.setEmployee(employee);
        bankAccount.setVerified(false);
        bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.PENDING);

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    public BankAccount addOrUpdateOrganizationBankAccount(Long orgId, BankAccount bankAccount, String username) {
        Organization organization = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (!organization.getAdminUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized access to organization resource");
        }

        if (organization.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization is not approved yet");
        }

        bankAccount.setOrganization(organization);
        bankAccount.setVerified(false);
        bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.PENDING);

        return bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void approveOrRejectOrganizationKyc(Long bankAccountId, boolean approve, String rejectionReason) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        if (bankAccount.getOrganization() == null) {
            throw new SecurityException("Bank account is not an organization account");
        }

        if (approve) {
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.VERIFIED);
            bankAccount.setVerified(true);
        } else {
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.REJECTED);
        }

        bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void approveOrRejectEmployeeKyc(Long orgId, Long bankAccountId, boolean approve, String rejectionReason, String username) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));

        if (bankAccount.getEmployee() == null || !bankAccount.getEmployee().getOrganization().getId().equals(orgId)) {
            throw new SecurityException("Bank account does not belong to specified organization");
        }

        Organization organization = bankAccount.getEmployee().getOrganization();
        if (!organization.getAdminUser().getUsername().equals(username)) {
            throw new SecurityException("Unauthorized to approve employee KYC for this organization");
        }

        if (approve) {
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.VERIFIED);
            bankAccount.setVerified(true);
        } else {
            bankAccount.setKycStatus(BankAccount.KYCDocumentVerificationStatus.REJECTED);
        }

        bankAccountRepository.save(bankAccount);
    }


    @Override
    public List<BankAccount> getBankAccountsForOrganization(Long organizationId) {
        return bankAccountRepository.findByOrganizationId(organizationId);
    }
    
    @Override
    public List<BankAccount> getBankAccountsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        return bankAccountRepository.findByEmployee(employee);
    }

    @Override
    public String getStatusByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        return getStatusByEmployee(employee);
    }

    // ✅ Get latest KYC status by Employee entity
    @Override
    public String getStatusByEmployee(Employee employee) {
        List<BankAccount> accounts = bankAccountRepository.findByEmployee(employee);
        if (accounts.isEmpty()) 
            return null;

        BankAccount latestAccount = accounts.stream()
                .max(Comparator.comparing(BankAccount::getId))
                .orElse(null);

        return (latestAccount != null && latestAccount.getKycStatus() != null)
                ? latestAccount.getKycStatus().name()
                : null;
    }


}
