package com.aurionpro.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.DocumentUploadDTO;
import com.aurionpro.dtos.OrganizationApprovalDTO;
import com.aurionpro.dtos.OrganizationRegistrationDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Organization;

import io.jsonwebtoken.io.IOException;

public interface OrganizationService {

    Organization getOrganizationById(Long orgId);

    Organization getOrganizationForAdmin(Long adminUserId);

    boolean isOrganizationApproved(Long orgId);

    void registerOrganization(OrganizationRegistrationDTO dto);

    List<Organization> getOrganizationsByStatus(Organization.Status status);

    void approveOrRejectOrganization(OrganizationApprovalDTO approvalDTO);

    Document uploadOrganizationDocument(Long orgId, String documentName, MultipartFile file) throws IOException, java.io.IOException;

    Document uploadOrganizationDocument(Long orgId, DocumentUploadDTO dto);

    List<Document> getOrganizationDocuments(Long orgId);
}
