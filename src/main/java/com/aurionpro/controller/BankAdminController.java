package com.aurionpro.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.DocumentResponseDTO;
import com.aurionpro.dtos.DocumentReviewDTO;
import com.aurionpro.dtos.OrganizationApprovalDTO;
import com.aurionpro.dtos.OrganizationResponseDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Organization;
import com.aurionpro.service.DocumentService;
import com.aurionpro.service.OrganizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-admin")
@RequiredArgsConstructor
@Validated
public class BankAdminController {

    private final OrganizationService organizationService;
    private final ModelMapper modelMapper;
    private final DocumentService documentService;
    
    @GetMapping("/organizations/pending")
    public ResponseEntity<List<OrganizationResponseDTO>> listPendingOrganizations() {
        List<Organization> pendingOrgs = organizationService.getOrganizationsByStatus(Organization.Status.PENDING);
        List<OrganizationResponseDTO> dtos = pendingOrgs.stream()
            .map(org -> modelMapper.map(org, OrganizationResponseDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    @PostMapping("/organizations/approval")
    public ResponseEntity<String> approveOrRejectOrganization(@Valid @RequestBody OrganizationApprovalDTO approvalDTO) {
        organizationService.approveOrRejectOrganization(approvalDTO);
        return ResponseEntity.ok("Organization approval decision applied successfully");
    }
    
    @GetMapping("/organizations/{orgId}/documents/pending")
    public ResponseEntity<List<DocumentResponseDTO>> getPendingDocuments(@PathVariable Long orgId) {
        List<Document> docs = documentService.getDocumentsPendingByOrganization(orgId);
        List<DocumentResponseDTO> dtos = docs.stream()
                .map(doc -> modelMapper.map(doc, DocumentResponseDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/documents/{docId}/verify")
    public ResponseEntity<String> verifyDocument(
            @PathVariable Long docId,
            @Valid @RequestBody DocumentReviewDTO reviewDTO) {
        documentService.verifyDocument(docId, reviewDTO.isApprove(), reviewDTO.getRejectionReason(), "BankAdmin");
        return ResponseEntity.ok("Document verification updated successfully");
    }
    
}
