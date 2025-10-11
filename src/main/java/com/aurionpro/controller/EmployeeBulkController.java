package com.aurionpro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.EmployeeBulkResultDTO;
import com.aurionpro.service.EmployeeBulkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/{orgId}/employee")
@RequiredArgsConstructor
@Validated
public class EmployeeBulkController {

    private final EmployeeBulkService employeeBulkService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')") // add org ownership SpEL if available
    @PostMapping(path = "/bulk-upload", consumes = "multipart/form-data")
    public ResponseEntity<List<EmployeeBulkResultDTO>> bulkUpload(
            @PathVariable Long orgId,
            @RequestPart("file") MultipartFile file) throws Exception {
        List<EmployeeBulkResultDTO> results = employeeBulkService.processCsv(orgId, file);
        return ResponseEntity.ok(results);
    }
}

