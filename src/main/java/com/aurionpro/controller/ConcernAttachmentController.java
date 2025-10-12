package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.AttachmentResponse;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.service.DocumentService;
import com.aurionpro.service.EmployeeService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/employees/{employeeId}/concerns")
public class ConcernAttachmentController {

    private final DocumentService documentService;
    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping(value = "/attachments", consumes = "multipart/form-data")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long orgId,
            @PathVariable Long employeeId,
            @RequestParam("file") @NotNull MultipartFile file
    ) {
        employeeService.assertEmployeeInOrg(employeeId, orgId);
        validateAttachment(file);

        AttachmentResponse res = documentService.storeEmployeeConcernAttachment(orgId, employeeId, file);
        return ResponseEntity.ok(res);
    }

    private void validateAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("File is required");
        }

        long maxBytes = 10L * 1024 * 1024; // 10 MB
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleException("File too large (max 10 MB)");
        }

        String ct = file.getContentType();
        boolean ok = MimeTypeUtils.IMAGE_PNG_VALUE.equals(ct)
                || MimeTypeUtils.IMAGE_JPEG_VALUE.equals(ct)
                || "application/pdf".equals(ct);

        if (!ok) {
            throw new BusinessRuleException("Unsupported file type");
        }
    }
}

