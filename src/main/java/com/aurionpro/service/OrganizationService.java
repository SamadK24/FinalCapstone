package com.aurionpro.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.DocumentUploadDTO;
import com.aurionpro.dtos.OrganizationApprovalDTO;
import com.aurionpro.dtos.OrganizationRegistrationDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.Role;
import com.aurionpro.entity.Role.RoleName;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.exceptions.UserAlreadyExistsException;
import com.aurionpro.repository.DocumentRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.RoleRepository;
import com.aurionpro.repository.UserRepository;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    
 // Inject DocumentRepository, CloudinaryService (or equivalent)
    private final DocumentRepository documentRepository;
    private final CloudinaryService cloudinaryService;
    
    private final EmailService emailService; // add this

    @Transactional
    public Document uploadOrganizationDocument(Long orgId, String documentName, MultipartFile file) throws IOException, java.io.IOException {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Upload file to cloudinary or storage service
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

        // Send email to org admin confirming registration
        emailService.sendOrganizationRegistered(
            adminUser.getEmail(),
            organization.getName(),
            adminUser.getUsername()
        );
    }

    public List<Organization> getOrganizationsByStatus(Organization.Status status) {
        return organizationRepository.findByStatus(status);
    }

    @Transactional
    public void approveOrRejectOrganization(OrganizationApprovalDTO approvalDTO) {
        Organization organization = organizationRepository.findById(approvalDTO.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

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

    public boolean isOrganizationApproved(Long orgId) {
        return organizationRepository.findById(orgId)
                .map(org -> org.getStatus() == Organization.Status.APPROVED)
                .orElse(false);
    }
    public Organization getOrganizationById(Long orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    @Transactional
    public Document uploadOrganizationDocument(Long orgId, DocumentUploadDTO dto) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Document document = Document.builder()
                .name(dto.getName())
                .fileType(extractFileType(dto.getFileUrl()))
                .filename(dto.getFileUrl())
                .organization(organization)
                .build();

        return documentRepository.save(document);
    }

    public List<Document> getOrganizationDocuments(Long orgId) {
        if (!organizationRepository.existsById(orgId)) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return documentRepository.findByOrganizationId(orgId);
    }

    private String extractFileType(String fileUrl) {
        // simple extraction from extension
        if (fileUrl != null && fileUrl.contains(".")) {
            return fileUrl.substring(fileUrl.lastIndexOf('.') + 1);
        }
        return "unknown";
    }
    
}
