package com.aurionpro.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.DocumentResponseDTO;
import com.aurionpro.dtos.OrganizationResponseDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Organization;
import com.aurionpro.service.CloudinaryService;
import com.aurionpro.service.DocumentService;
import com.aurionpro.service.OrganizationService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/{orgId}/documents")
@RequiredArgsConstructor
@Validated
public class OrganizationDocumentsController {

	private final OrganizationService organizationService;
    private final DocumentService documentService;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') and @securityService.isOrgAdmin(#orgId, authentication.name)")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> uploadDocument(
            @PathVariable Long orgId,
            @RequestParam @NotBlank String documentName,
            @RequestPart("file") MultipartFile file) throws IOException {

        Organization org = organizationService.getOrganizationById(orgId);

        String fileUrl = cloudinaryService.uploadFile(file);

        Document doc = documentService.saveOrganizationDocument(org, documentName, file.getOriginalFilename(), file.getContentType(), fileUrl);

        DocumentResponseDTO dto = modelMapper.map(doc, DocumentResponseDTO.class);
        return ResponseEntity.ok(dto);
    }
    
    @PreAuthorize("hasRole('BANK_ADMIN')")
    @GetMapping("/organizations/pending")
    public ResponseEntity<List<OrganizationResponseDTO>> listPendingOrganizations() {
        List<Organization> pendingOrgs = organizationService.getOrganizationsByStatus(Organization.Status.PENDING);
        List<OrganizationResponseDTO> dtos = pendingOrgs.stream()
            .map(org -> modelMapper.map(org, OrganizationResponseDTO.class))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
