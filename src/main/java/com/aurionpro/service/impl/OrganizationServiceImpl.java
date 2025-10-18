package com.aurionpro.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.DocumentUploadDTO;
import com.aurionpro.dtos.OrganizationApprovalDTO;
import com.aurionpro.dtos.OrganizationRegistrationDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.Organization.Status;
import com.aurionpro.entity.Role;
import com.aurionpro.entity.Role.RoleName;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.exceptions.UserAlreadyExistsException;
import com.aurionpro.repository.DocumentRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.RoleRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.CloudinaryService;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.OrganizationService;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final DocumentRepository documentRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;

    @Override
    @Transactional
    public Document uploadOrganizationDocument(Long orgId, String documentName, MultipartFile file) throws IOException, java.io.IOException {
        Organization organization = getOrganizationById(orgId);

        String fileUrl = cloudinaryService.uploadFile(file);

        Document document = Document.builder()
                .name(documentName)
                .filename(file.getOriginalFilename())
                .fileType(file.getContentType())
                .url(fileUrl)
                .organization(organization)
                .build();

        return documentRepository.save(document);
    }

    @Override
    public Organization getOrganizationForAdmin(Long adminUserId) {
        return organizationRepository.findByAdminUserId(adminUserId)
              .orElse(null);
    }

    @Override
    @Transactional
    public void registerOrganization(OrganizationRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.getAdminUsername()))
            throw new UserAlreadyExistsException("Admin username is already taken");

        if (userRepository.existsByEmail(dto.getAdminEmail()))
            throw new UserAlreadyExistsException("Admin email is already in use");

        User adminUser = new User();
        adminUser.setUsername(dto.getAdminUsername());
        adminUser.setEmail(dto.getAdminEmail());
        adminUser.setPassword(passwordEncoder.encode(dto.getAdminPassword()));

        Role organizationAdminRole = roleRepository.findByName(RoleName.ROLE_ORGANIZATION_ADMIN)
                .orElseThrow(() -> new RuntimeException("Organization Admin Role not found"));

        adminUser.setRoles(new HashSet<>(Collections.singletonList(organizationAdminRole)));
        userRepository.save(adminUser);

        Organization organization = Organization.builder()
                .name(dto.getName())
                .contactNumber(dto.getContactNumber())
                .address(dto.getAddress())
                .adminUser(adminUser)
                .status(Organization.Status.PENDING)
                .build();

        organizationRepository.save(organization);

        emailService.sendOrganizationRegistered(
            adminUser.getEmail(),
            organization.getName(),
            adminUser.getUsername()
        );
    }

    @Override
    public List<Organization> getOrganizationsByStatus(Organization.Status status) {
        return organizationRepository.findByStatus(status);
    }

    @Override
    public Page<Organization> getOrganizationsByStatus(Organization.Status status, Pageable pageable) {
        return organizationRepository.findByStatus(status, pageable);
    }


    @Override
    @Transactional
    public void approveOrRejectOrganization(OrganizationApprovalDTO approvalDTO) {
        Organization organization = getOrganizationById(approvalDTO.getOrganizationId());

        if (approvalDTO.getApprove()) {
            if (organization.getDocuments() == null || organization.getDocuments().isEmpty()) {
                throw new IllegalStateException("Organization has not uploaded mandatory documents");
            }
            organization.setStatus(Organization.Status.APPROVED);
            organizationRepository.save(organization);

            String toEmail = organization.getAdminUser().getEmail();
            emailService.sendOrganizationApproved(toEmail, organization.getName());
        } else {
            organization.setStatus(Organization.Status.REJECTED);
            organizationRepository.save(organization);

            String toEmail = organization.getAdminUser().getEmail();
            emailService.sendOrganizationRejected(toEmail, organization.getName(), approvalDTO.getRejectionReason());
        }
    }

    @Override
    public boolean isOrganizationApproved(Long orgId) {
        return organizationRepository.findById(orgId)
                .map(org -> org.getStatus() == Organization.Status.APPROVED)
                .orElse(false);
    }

    @Override
    public Organization getOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    @Override
    @Transactional
    public Document uploadOrganizationDocument(Long orgId, DocumentUploadDTO dto) {
        Organization organization = getOrganizationById(orgId);

        Document document = Document.builder()
                .name(dto.getName())
                .fileType(extractFileType(dto.getFileUrl()))
                .filename(dto.getFileUrl())
                .organization(organization)
                .build();

        return documentRepository.save(document);
    }

    @Override
    public List<Document> getOrganizationDocuments(Long orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return documentRepository.findByOrganizationId(orgId);
    }

    private String extractFileType(String fileUrl) {
        if (fileUrl != null && fileUrl.contains(".")) {
            return fileUrl.substring(fileUrl.lastIndexOf('.') + 1);
        }
        return "unknown";
    }



	
}

