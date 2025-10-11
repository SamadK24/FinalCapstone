package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.aurionpro.service.VendorPaymentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class VendorPaymentExecutionController {

    private final VendorPaymentService vendorPaymentService;

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/vendor-payments/batches/{batchId}/execute")
    public ResponseEntity<VendorPaymentService.ExecutionSummary> execute(@PathVariable Long batchId) {
        return ResponseEntity.ok(vendorPaymentService.executeApprovedVendorBatch(batchId));
    }
}

