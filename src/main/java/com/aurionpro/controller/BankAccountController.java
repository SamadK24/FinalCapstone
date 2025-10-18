package com.aurionpro.controller;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.BankAccountDTO;
import com.aurionpro.entity.BankAccount;
import com.aurionpro.service.BankAccountService;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
@Validated

public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final ModelMapper modelMapper;


    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or (hasRole('EMPLOYEE') and @securityService.isEmployeeSelf(#employeeId, authentication.name))")
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<BankAccountDTO> addEmployeeBankAccount(
            @PathVariable Long employeeId,
            @Valid @RequestBody BankAccountDTO bankAccountDTO) {
        BankAccount bankAccount = modelMapper.map(bankAccountDTO, BankAccount.class);
        BankAccount savedAccount = bankAccountService.addOrUpdateEmployeeBankAccount(employeeId, bankAccount);
        return ResponseEntity.ok(modelMapper.map(savedAccount, BankAccountDTO.class));
    }


    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') and @securityService.isOrgAdmin(#orgId, authentication.name)")
    @PostMapping("/organization/{orgId}")
    public ResponseEntity<BankAccountDTO> addOrganizationBankAccount(
            @PathVariable Long orgId,
            @Valid @RequestBody BankAccountDTO bankAccountDTO,
            @AuthenticationPrincipal UserDetails currentUser) {
        BankAccount bankAccount = modelMapper.map(bankAccountDTO, BankAccount.class);
        BankAccount savedAccount = bankAccountService.addOrUpdateOrganizationBankAccount(orgId, bankAccount, currentUser.getUsername());
        return ResponseEntity.ok(modelMapper.map(savedAccount, BankAccountDTO.class));
    }
    

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or (hasRole('EMPLOYEE') and @securityService.isEmployeeSelf(#employeeId, authentication.name))")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<BankAccountDTO>> listEmployeeBankAccounts(
           @PathVariable Long employeeId) {
        List<BankAccount> accounts = bankAccountService.getBankAccountsForEmployee(employeeId);
        List<BankAccountDTO> dtoList = accounts.stream()
                .map(acc -> modelMapper.map(acc, BankAccountDTO.class))
                .toList();
        return ResponseEntity.ok(dtoList);
    }


    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/{bankAccountId}/kyc-approval")
    public ResponseEntity<String> approveOrRejectOrganizationBankAccountKyc(
            @PathVariable Long bankAccountId,
             @RequestParam boolean approve,
            @RequestParam(required = false) String rejectionReason) {
        bankAccountService.approveOrRejectOrganizationKyc(bankAccountId, approve, rejectionReason);
        String message = approve ? "Organization bank account KYC approved successfully" 
                                 : "Organization bank account KYC rejected";
        return ResponseEntity.ok(message);
    }

 
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') and @securityService.isOrgAdminForEmployeeBankAccount(#orgId, #bankAccountId, authentication.name)")
    @PostMapping("/organization/{orgId}/employee-bank-accounts/{bankAccountId}/kyc-approval")
    public ResponseEntity<String> approveOrRejectEmployeeBankAccountKyc(
           @PathVariable Long orgId,
           @PathVariable Long bankAccountId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectionReason,
            @AuthenticationPrincipal UserDetails currentUser) {
        bankAccountService.approveOrRejectEmployeeKyc(orgId, bankAccountId, approve, rejectionReason, currentUser.getUsername());
        String message = approve ? "Employee bank account KYC approved successfully" 
                                 : "Employee bank account KYC rejected";
        return ResponseEntity.ok(message);
    }

 
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') and @securityService.isOrgAdmin(#orgId, authentication.name)")
    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<BankAccountDTO>> listOrganizationBankAccounts(
    @PathVariable Long orgId) {
        List<BankAccount> accounts = bankAccountService.getBankAccountsForOrganization(orgId);
        List<BankAccountDTO> dtoList = accounts.stream()
                .map(acc -> modelMapper.map(acc, BankAccountDTO.class))
                .toList();
        return ResponseEntity.ok(dtoList);
    }
}
