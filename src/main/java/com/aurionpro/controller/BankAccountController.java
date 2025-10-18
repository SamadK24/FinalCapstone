package com.aurionpro.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.aurionpro.repository.BankAccountRepository;
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
    private final BankAccountRepository bankAccountRepository;
    
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/employee/{employeeId}")
    public ResponseEntity<BankAccountDTO> addEmployeeBankAccount(@PathVariable Long employeeId,
                                                                @Valid @RequestBody BankAccountDTO bankAccountDTO) {
        BankAccount bankAccount = modelMapper.map(bankAccountDTO, BankAccount.class);
        BankAccount savedAccount = bankAccountService.addOrUpdateEmployeeBankAccount(employeeId, bankAccount);
        return ResponseEntity.ok(modelMapper.map(savedAccount, BankAccountDTO.class));
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/organization/{orgId}")
    public ResponseEntity<BankAccountDTO> addOrganizationBankAccount(@PathVariable Long orgId,
                                                                    @Valid @RequestBody BankAccountDTO bankAccountDTO,
                                                                    @AuthenticationPrincipal UserDetails currentUser) {
        BankAccount bankAccount = modelMapper.map(bankAccountDTO, BankAccount.class);
        BankAccount savedAccount = bankAccountService.addOrUpdateOrganizationBankAccount(orgId, bankAccount, currentUser.getUsername());
        return ResponseEntity.ok(modelMapper.map(savedAccount, BankAccountDTO.class));
    }
    
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<BankAccountDTO>> listEmployeeBankAccounts(@PathVariable Long employeeId) {
        List<BankAccount> accounts = bankAccountService.getBankAccountsForEmployee(employeeId);
        List<BankAccountDTO> dtoList = accounts.stream()
                .map(acc -> modelMapper.map(acc, BankAccountDTO.class))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/{bankAccountId}/kyc-approval")
    public ResponseEntity<String> approveOrRejectOrganizationBankAccountKyc(@PathVariable Long bankAccountId,
                                                                            @RequestParam boolean approve,
                                                                            @RequestParam(required = false) String rejectionReason) {
        bankAccountService.approveOrRejectOrganizationKyc(bankAccountId, approve, rejectionReason);
        return ResponseEntity.ok("Organization bank account KYC status updated successfully");
    }

    // Employee bank account KYC approval — accessible only by Organization Admin of that org ----changes by durgesh
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/organization/{orgId}/employee-bank-accounts/{bankAccountId}/kyc-approval")
    public ResponseEntity<Map<String, String>> approveOrRejectEmployeeBankAccountKyc(
            @PathVariable Long orgId,
            @PathVariable Long bankAccountId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String rejectionReason,
            @AuthenticationPrincipal UserDetails currentUser) {
        
        bankAccountService.approveOrRejectEmployeeKyc(orgId, bankAccountId, approve, rejectionReason, currentUser.getUsername());
        return ResponseEntity.ok(Map.of("message", "Employee bank account KYC status updated successfully"));
    }



    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/organization/{orgId}")
    public ResponseEntity<List<BankAccountDTO>> listOrganizationBankAccounts(@PathVariable Long orgId) {
        List<BankAccount> accounts = bankAccountService.getBankAccountsForOrganization(orgId);
        List<BankAccountDTO> dtoList = accounts.stream()
                .map(acc -> modelMapper.map(acc, BankAccountDTO.class))
                .toList();
        return ResponseEntity.ok(dtoList);
    }
    
    //return all bank account of employees for kyc -durgesh changes 
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/organization/{orgId}/employees")
    public ResponseEntity<List<BankAccountDTO>> listEmployeeBankAccountsForOrganization(@PathVariable Long orgId) {
        List<BankAccount> accounts = bankAccountService.getEmployeeBankAccountsForOrganization(orgId);
        List<BankAccountDTO> dtoList = accounts.stream()
                .map(acc -> modelMapper.map(acc, BankAccountDTO.class))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

 // New endpoint for Bank Admin to get pending organization bank accounts --durgesh changes
    @PreAuthorize("hasRole('BANK_ADMIN')")
    @GetMapping("/bank-admin/organization-bank-accounts/pending")
    public ResponseEntity<List<Map<String, Object>>> listPendingOrgBankAccounts() {
        List<BankAccount> accounts = bankAccountRepository.findAll().stream()
                .filter(ba -> ba.getOrganization() != null && 
                             ba.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.PENDING)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> result = accounts.stream()
                .map(acc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", acc.getId());
                    map.put("accountHolderName", acc.getAccountHolderName());
                    map.put("accountNumber", acc.getAccountNumber());
                    map.put("ifscCode", acc.getIfscCode());
                    map.put("bankName", acc.getBankName());
                    map.put("verified", acc.isVerified());
                    map.put("kycStatus", acc.getKycStatus().name());
                    if (acc.getOrganization() != null) {
                        map.put("organizationId", acc.getOrganization().getId());
                        map.put("organizationName", acc.getOrganization().getName());
                    }
                    return map;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }


}
