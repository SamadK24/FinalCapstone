package com.aurionpro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.SalaryTemplateDTO;
import com.aurionpro.service.SalaryTemplateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/{orgId}/salary-templates")
@RequiredArgsConstructor
@Validated
public class SalaryTemplateController {

    private final SalaryTemplateService templateService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or hasRole('HR')")
    @PostMapping
    public ResponseEntity<SalaryTemplateDTO> createOrUpdateTemplate(@PathVariable Long orgId,
                                                                    @Valid @RequestBody SalaryTemplateDTO templateDTO) {
        SalaryTemplateDTO responseDTO = templateService.createOrUpdateTemplate(orgId, templateDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or hasRole('HR')")
    @GetMapping
    public ResponseEntity<List<SalaryTemplateDTO>> getTemplates(@PathVariable Long orgId) {
        List<SalaryTemplateDTO> templates = templateService.getTemplatesByOrganization(orgId);
        return ResponseEntity.ok(templates);
    }
}
