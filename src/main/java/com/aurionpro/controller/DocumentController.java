package com.aurionpro.controller;

import java.nio.file.Paths;
import java.util.List;

import org.modelmapper.ModelMapper;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.DocumentResponseDTO;
import com.aurionpro.dtos.DocumentReviewDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.exceptions.InvalidFileException;
import com.aurionpro.exceptions.InvalidInputException;
import com.aurionpro.exceptions.OrganizationNotFoundException;
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

        // ---------------- 1️⃣ Validate Organization ----------------
        Organization org = organizationService.getOrganizationById(orgId);
        if (org == null) {
            throw new OrganizationNotFoundException("Organization with ID " + orgId + " does not exist");
        }

        // ---------------- 2️⃣ Validate Employee (optional) ----------------
        Employee emp = null;
        if (employeeId != null) {
            emp = employeeService.getEmployeeById(employeeId);
            if (emp == null) {
                throw new InvalidInputException("Employee with ID " + employeeId + " does not exist");
            }
        }

        // ---------------- 3️⃣ Validate Document Name ----------------
        if (documentName == null || documentName.trim().length() < 3 || documentName.trim().length() > 100) {
            throw new InvalidInputException("Document name must be between 3 and 100 characters");
        }
        if (!documentName.matches("^[a-zA-Z0-9 _-]+$")) {
            throw new InvalidInputException("Document name can only contain letters, numbers, spaces, hyphens, and underscores");
        }

        // ---------------- 4️⃣ Validate File ----------------
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is mandatory");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png"
        );

        if (!allowedTypes.contains(contentType)) {
            throw new InvalidFileException("Unsupported file type: " + contentType);
        }

        long maxSize = 5 * 1024 * 1024; // 5 MB
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("File size exceeds 5 MB");
        }

        // Sanitize filename to prevent path traversal
        String safeFilename = Paths.get(file.getOriginalFilename()).getFileName().toString();

        // ---------------- 5️⃣ Upload File ----------------
        String fileUrl = cloudinaryService.uploadFile(file);

        // ---------------- 6️⃣ Save Document ----------------
        Document doc = documentService.saveDocument(
            org,
            emp,
            documentName.trim(),
            safeFilename,
            contentType,
            fileUrl
        );

        // ---------------- 7️⃣ Map to DTO and return ----------------
        DocumentResponseDTO dto = modelMapper.map(doc, DocumentResponseDTO.class);
        return ResponseEntity.ok(dto);
    }


    @PreAuthorize("hasRole('BANK_ADMIN') or hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<Page<DocumentResponseDTO>> listPendingDocuments(Pageable pageable) {
        Page<DocumentResponseDTO> dtos = documentService.getPendingDocuments(pageable);
        return ResponseEntity.ok(dtos);
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/{orgId}/pending")
    public ResponseEntity<Page<DocumentResponseDTO>> listOrganizationPendingDocuments(
            @PathVariable Long orgId,
            Pageable pageable) {
        Page<DocumentResponseDTO> dtos = documentService.getPendingDocumentsByOrganization(orgId, pageable);
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
