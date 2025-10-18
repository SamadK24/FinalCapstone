package com.aurionpro.service;



import org.springframework.stereotype.Service;

import com.aurionpro.repository.BankAccountRepository;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final BankAccountRepository bankAccountRepository;

    public boolean isOrgAdmin(Long orgId, String username) {
        return organizationRepository.findById(orgId)
            .map(org -> org.getAdminUser().getUsername().equals(username))
            .orElse(false);
    }

    public boolean isEmployeeSelf(Long employeeId, String username) {
        return employeeRepository.findById(employeeId)
            .map(emp -> emp.getUserAccount() != null && emp.getUserAccount().getUsername().equals(username))
            .orElse(false);
    }

    public boolean isOrgAdminForEmployeeBankAccount(Long orgId, Long bankAccountId, String username) {
        return bankAccountRepository.findById(bankAccountId)
            .map(account -> account.getEmployee() != null 
                && account.getEmployee().getOrganization().getId().equals(orgId)
                && account.getEmployee().getOrganization().getAdminUser().getUsername().equals(username))
            .orElse(false);
    }
}