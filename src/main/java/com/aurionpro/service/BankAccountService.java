package com.aurionpro.service;

import java.util.List;

import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Employee;

public interface BankAccountService {

    BankAccount addOrUpdateEmployeeBankAccount(Long employeeId, BankAccount bankAccount);

    BankAccount addOrUpdateOrganizationBankAccount(Long orgId, BankAccount bankAccount, String username);

    void approveOrRejectOrganizationKyc(Long bankAccountId, boolean approve, String rejectionReason);

    void approveOrRejectEmployeeKyc(Long orgId, Long bankAccountId, boolean approve, String rejectionReason, String username);

    List<BankAccount> getBankAccountsForOrganization(Long organizationId);
    List<BankAccount> getBankAccountsForEmployee(Long employeeId);
    String getStatusByEmployee(Long employeeId);

    // ✅ Get KYC status by employee entity
    String getStatusByEmployee(Employee employee);
    

}
