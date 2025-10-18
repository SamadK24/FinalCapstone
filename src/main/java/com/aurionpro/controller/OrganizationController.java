package com.aurionpro.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ChangePasswordRequest;
import com.aurionpro.dtos.UpdateOrgProfileRequest;
import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Organization;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.service.OrganizationProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationProfileService organizationProfileService;
    private final OrganizationRepository organizationRepository;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PatchMapping("/{orgId}/profile")
    public ResponseEntity<Void> update(
        @PathVariable Long orgId,
        @Valid @RequestBody UpdateOrgProfileRequest req
    ) {
        organizationProfileService.updateProfile(orgId, req);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long orgId,
        @Valid @RequestBody ChangePasswordRequest req
    ) {
        organizationProfileService.changePassword(orgId, req.getCurrentPassword(), req.getNewPassword(), req.getConfirmNewPassword());
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/{orgId}")
    public ResponseEntity<Map<String, Object>> getOrganizationDetails(@PathVariable Long orgId) {
        Organization org = organizationRepository.findById(orgId)
            .orElseThrow(() -> new RuntimeException("Organization not found with id: " + orgId));

        // Calculate total balance from all VERIFIED payroll bank accounts
        BigDecimal totalBalance = org.getPayrollBankAccounts().stream()
            .filter(ba -> ba.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.VERIFIED)
            .map(BankAccount::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("id", org.getId());
        response.put("name", org.getName());
        response.put("balance", totalBalance);
        response.put("status", org.getStatus().toString());
        response.put("contactNumber", org.getContactNumber());
        response.put("address", org.getAddress());

        return ResponseEntity.ok(response);
    }
}

