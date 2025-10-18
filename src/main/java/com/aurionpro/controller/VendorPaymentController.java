package com.aurionpro.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.aurionpro.dtos.PaymentBatchApprovalDTO;
import com.aurionpro.dtos.PaymentBatchCreateDTO;
import com.aurionpro.dtos.PaymentBatchResponseDTO;
import com.aurionpro.service.VendorPaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class VendorPaymentController {

    private final VendorPaymentService vendorPaymentService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/organization/{orgId}/vendor-payments/batches")
    public ResponseEntity<PaymentBatchResponseDTO> createBatch(
            @PathVariable Long orgId,
            @Valid @RequestBody PaymentBatchCreateDTO dto,
            @AuthenticationPrincipal UserDetails currentUser) {
        String createdBy = currentUser != null ? currentUser.getUsername() : "system";
        return ResponseEntity.ok(vendorPaymentService.createVendorPaymentBatch(orgId, dto, createdBy));
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @GetMapping("/bank-admin/vendor-payments/batches/pending")
    public ResponseEntity<Page<PaymentBatchResponseDTO>> listPendingBatches(Pageable pageable) {
        return ResponseEntity.ok(vendorPaymentService.listPendingForBankAdmin(pageable));
    }


    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/vendor-payments/batches/review")
    public ResponseEntity<String> reviewBatch(@Valid @RequestBody PaymentBatchApprovalDTO dto) {
        vendorPaymentService.reviewVendorBatch(dto);
        return ResponseEntity.ok("Vendor payment batch review applied successfully");
    }
}

