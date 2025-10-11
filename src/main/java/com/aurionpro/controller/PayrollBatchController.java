package com.aurionpro.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.aurionpro.dtos.DisbursalBatchApprovalDTO;
import com.aurionpro.dtos.DisbursalBatchCreateDTO;
import com.aurionpro.dtos.DisbursalBatchResponseDTO;
import com.aurionpro.service.PayrollBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class PayrollBatchController {

    private final PayrollBatchService payrollBatchService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/organization/{orgId}/payroll/batches")
    public ResponseEntity<DisbursalBatchResponseDTO> createBatch(
            @PathVariable Long orgId,
            @Valid @RequestBody DisbursalBatchCreateDTO dto,
            @AuthenticationPrincipal UserDetails currentUser) {
        String createdBy = currentUser != null ? currentUser.getUsername() : "system";
        return ResponseEntity.ok(payrollBatchService.createBatch(orgId, dto, createdBy));
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @GetMapping("/bank-admin/payroll/batches/pending")
    public ResponseEntity<List<DisbursalBatchResponseDTO>> listPendingBatches() {
        return ResponseEntity.ok(payrollBatchService.listPendingBatchesForBankAdmin());
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/payroll/batches/review")
    public ResponseEntity<String> reviewBatch(@Valid @RequestBody DisbursalBatchApprovalDTO dto) {
        payrollBatchService.reviewBatch(dto);
        return ResponseEntity.ok("Batch review decision applied successfully");
    }
}

