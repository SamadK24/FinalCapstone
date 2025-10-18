package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.service.PayrollBatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated

public class PayrollExecutionController {

    private final PayrollBatchService payrollBatchService;


    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/payroll/batches/{batchId}/execute")
    public ResponseEntity<PayrollBatchService.ExecutionSummary> executeBatch(
           @PathVariable Long batchId) {
        PayrollBatchService.ExecutionSummary summary = payrollBatchService.executeApprovedBatch(batchId);
        return ResponseEntity.ok(summary);
    }
}
