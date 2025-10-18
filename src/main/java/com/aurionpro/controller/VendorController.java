package com.aurionpro.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.aurionpro.dtos.*;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.Vendor;
import com.aurionpro.entity.Vendor.Status;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.VendorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/{orgId}/vendors")
@RequiredArgsConstructor
@Validated
public class VendorController {

    private final OrganizationRepository organizationRepository;
    private final VendorRepository vendorRepository;
    private final ModelMapper modelMapper;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') and @securityService.isOrgAdmin(#orgId, authentication.name)")
    @PostMapping
    public ResponseEntity<VendorResponseDTO> createVendor(@PathVariable Long orgId, @Valid @RequestBody VendorCreateDTO dto) {
        // 1. Validate organization exists and is approved
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));
        
        if (org.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization must be approved to create vendors");
        }
        
        // 2. Check for duplicate vendor name
        if (vendorRepository.existsByOrganizationIdAndName(orgId, dto.getName())) {
            throw new IllegalArgumentException("Vendor already exists with name: " + dto.getName());
        }
        
        // 3. Validate bank details if provided
        if (dto.getAccountNumber() != null) {
            if (!dto.getAccountNumber().matches("\\d{9,18}")) {
                throw new IllegalArgumentException("Invalid account number format. Must be 9-18 digits.");
            }
        }
        
        if (dto.getIfscCode() != null) {
            if (!dto.getIfscCode().matches("[A-Z]{4}0[A-Z0-9]{6}")) {
                throw new IllegalArgumentException("Invalid IFSC code format");
            }
        }

        Vendor v = Vendor.builder()
                .organization(org)
                .name(dto.getName())
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .accountHolderName(dto.getAccountHolderName())
                .accountNumber(dto.getAccountNumber())
                .ifscCode(dto.getIfscCode())
                .bankName(dto.getBankName())
                .documentRefs(dto.getDocumentRefs())
                .build();

        Vendor saved = vendorRepository.save(v);
        return ResponseEntity.ok(toResponse(saved));
    }


    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping
    public ResponseEntity<List<VendorResponseDTO>> listVendors(@PathVariable Long orgId) {
        List<VendorResponseDTO> out = vendorRepository.findByOrganizationId(orgId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PutMapping("/{vendorId}")
    public ResponseEntity<VendorResponseDTO> updateVendor(@PathVariable Long orgId, @PathVariable Long vendorId,
                                                          @Valid @RequestBody VendorUpdateDTO dto) {
        Vendor v = vendorRepository.findByIdAndOrganizationId(vendorId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        v.setName(dto.getName());
        v.setContactEmail(dto.getContactEmail());
        v.setContactPhone(dto.getContactPhone());
        v.setAccountHolderName(dto.getAccountHolderName());
        v.setAccountNumber(dto.getAccountNumber());
        v.setIfscCode(dto.getIfscCode());
        v.setBankName(dto.getBankName());
        v.setDocumentRefs(dto.getDocumentRefs());
        if (dto.getStatus() != null) {
            v.setStatus(Status.valueOf(dto.getStatus()));
        }

        Vendor saved = vendorRepository.save(v);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @DeleteMapping("/{vendorId}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long orgId, @PathVariable Long vendorId) {
        Vendor v = vendorRepository.findByIdAndOrganizationId(vendorId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendorRepository.delete(v);
        return ResponseEntity.noContent().build();
    }

    private VendorResponseDTO toResponse(Vendor v) {
        return VendorResponseDTO.builder()
                .id(v.getId())
                .organizationId(v.getOrganization().getId())
                .name(v.getName())
                .contactEmail(v.getContactEmail())
                .contactPhone(v.getContactPhone())
                .accountHolderName(v.getAccountHolderName())
                .accountNumber(v.getAccountNumber())
                .ifscCode(v.getIfscCode())
                .bankName(v.getBankName())
                .kycStatus(v.getKycStatus().name())
                .status(v.getStatus().name())
                .documentRefs(v.getDocumentRefs())
                .build();
    }
}

