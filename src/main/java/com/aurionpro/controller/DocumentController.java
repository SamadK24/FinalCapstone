package com.aurionpro.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.DocumentResponseDTO;
import com.aurionpro.dtos.DocumentReviewDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.service.CloudinaryService;
import com.aurionpro.service.DocumentService;
import com.aurionpro.service.EmployeeService;
import com.aurionpro.service.OrganizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Validated
public class DocumentController {

    private final DocumentService documentService;
    private final OrganizationService organizationService;
    private final EmployeeService employeeService;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ORGANIZATION_ADMIN')")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @RequestParam Long orgId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam String documentName,
            @RequestPart MultipartFile file) throws Exception {

        Organization org = organizationService.getOrganizationById(orgId);
        Employee emp = null;
        if (employeeId != null) {
            emp = employeeService.getEmployeeById(employeeId);
        }

        String fileUrl = cloudinaryService.uploadFile(file);
        Document doc = documentService.saveDocument(org, emp, documentName, file.getOriginalFilename(), file.getContentType(), fileUrl);
        return ResponseEntity.ok(modelMapper.map(doc, DocumentResponseDTO.class));
    }

    @PreAuthorize("hasRole('BANK_ADMIN') or hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<DocumentResponseDTO>> listPendingDocuments() {
        List<Document> pendingDocs = documentService.getDocumentsForVerificationPending();
        List<DocumentResponseDTO> dtos = pendingDocs.stream()
                .map(doc -> modelMapper.map(doc, DocumentResponseDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('BANK_ADMIN') or hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/{id}/review")
    public ResponseEntity<String> reviewDocument(@PathVariable Long id,
                                                 @Valid @RequestBody DocumentReviewDTO dto,
                                                 @AuthenticationPrincipal UserDetails currentUser) {
        documentService.verifyDocument(id, dto.isApprove(), dto.getRejectionReason(), currentUser.getUsername());
        return ResponseEntity.ok("Document reviewed successfully");
    }
}
