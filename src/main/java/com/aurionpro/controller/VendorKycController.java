package com.aurionpro.controller;

import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.aurionpro.dtos.VendorKycUpdateDTO;
import com.aurionpro.entity.BankAccount.KYCDocumentVerificationStatus;
import com.aurionpro.entity.Vendor;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.VendorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-admin/vendors")
@RequiredArgsConstructor
@Validated
public class VendorKycController {

    private final VendorRepository vendorRepository;

    @PreAuthorize("hasRole('BANK_ADMIN')")
    @PostMapping("/{vendorId}/kyc")
    public ResponseEntity<?> updateKyc(@PathVariable Long vendorId,
                                       @Valid @RequestBody VendorKycUpdateDTO dto,
                                       @AuthenticationPrincipal UserDetails principal) {

        Vendor v = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        KYCDocumentVerificationStatus newStatus;
        try {
            newStatus = KYCDocumentVerificationStatus.valueOf(dto.getStatus());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid KYC status. Use VERIFIED or REJECTED.");
        }

        if (newStatus != KYCDocumentVerificationStatus.VERIFIED
                && newStatus != KYCDocumentVerificationStatus.REJECTED) {
            return ResponseEntity.badRequest().body("KYC status must be VERIFIED or REJECTED.");
        }

        v.setKycStatus(newStatus);
        v.setLastKycReviewedAt(Instant.now());
        v.setLastKycReviewedBy(principal != null ? principal.getUsername() : "system");
        v.setKycReviewReason(dto.getReason());

        vendorRepository.save(v);
        return ResponseEntity.ok("Vendor KYC updated to " + newStatus.name());
    }
}

