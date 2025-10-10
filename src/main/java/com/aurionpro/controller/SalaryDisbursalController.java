package com.aurionpro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.SalaryDisbursalApprovalDTO;
import com.aurionpro.dtos.SalaryDisbursalRequestDTO;
import com.aurionpro.dtos.SalaryDisbursalResponseDTO;
import com.aurionpro.service.SalaryDisbursalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class SalaryDisbursalController {

    private final SalaryDisbursalService disbursalService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/organization/{orgId}/salary-disbursal-request")
    public ResponseEntity<SalaryDisbursalResponseDTO> createDisbursalRequest(
            @PathVariable Long orgId,
            @Valid @RequestBody SalaryDisbursalRequestDTO requestDTO) {
        SalaryDisbursalResponseDTO responseDTO = disbursalService.createSalaryDisbursalRequest(orgId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @GetMapping("/bank-admin/salary-disbursal-requests/pending")
    public ResponseEntity<List<SalaryDisbursalResponseDTO>> getPendingRequests() {
        List<SalaryDisbursalResponseDTO> pendingRequests = disbursalService.getPendingRequestsForBankAdmin();
        return ResponseEntity.ok(pendingRequests);
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/salary-disbursal-requests/approval")
    public ResponseEntity<String> approveOrRejectSalaryDisbursal(@Valid @RequestBody SalaryDisbursalApprovalDTO approvalDTO) {
        disbursalService.approveOrRejectRequest(approvalDTO);
        return ResponseEntity.ok("Salary disbursal approval decision applied successfully");
    }
}
