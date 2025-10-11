package com.aurionpro.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<List<PaymentBatchResponseDTO>> listPendingBatches() {
        return ResponseEntity.ok(vendorPaymentService.listPendingForBankAdmin());
    }

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/bank-admin/vendor-payments/batches/review")
    public ResponseEntity<String> reviewBatch(@Valid @RequestBody PaymentBatchApprovalDTO dto) {
        vendorPaymentService.reviewVendorBatch(dto);
        return ResponseEntity.ok("Vendor payment batch review applied successfully");
    }
}

