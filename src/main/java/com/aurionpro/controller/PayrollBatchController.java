package com.aurionpro.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.DisbursalBatchApprovalDTO;
import com.aurionpro.dtos.DisbursalBatchCreateDTO;
import com.aurionpro.dtos.DisbursalBatchResponseDTO;
import com.aurionpro.dtos.DisbursalBatchResponseDTO.DisbursalLineDTO;
import com.aurionpro.entity.DisbursalBatch;
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
    @PostMapping("/bank-admin/payroll/batches/{batchId}/execute")
    public ResponseEntity<Map<String, Object>> executeBatch(@PathVariable Long batchId) {
        try {
            PayrollBatchService.ExecutionSummary summary = payrollBatchService.executeApprovedBatch(batchId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Batch execution completed");
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to execute batch: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @GetMapping("/bank-admin/payroll/batches/approved")
    public ResponseEntity<List<DisbursalBatchResponseDTO>> listApprovedBatches() {
        List<DisbursalBatchResponseDTO> batches = payrollBatchService.listApprovedBatches();
        return ResponseEntity.ok(batches);
    }

    
    

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/payroll/batches/review")
    public ResponseEntity<String> reviewBatch(@Valid @RequestBody DisbursalBatchApprovalDTO dto) {
        payrollBatchService.reviewBatch(dto);
        return ResponseEntity.ok("Batch review decision applied successfully");
    }
}

