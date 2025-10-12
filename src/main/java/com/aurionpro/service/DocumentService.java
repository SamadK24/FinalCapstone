package com.aurionpro.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.AttachmentResponse;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.DocumentRepository;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final CloudinaryService cloudinaryService;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService; // add this
    
    @org.springframework.beans.factory.annotation.Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;
    
    @Transactional
    public Document saveEmployeeDocument(Long orgId, Long empId, String name, String filename, String mimeType, MultipartFile file) throws IOException, java.io.IOException {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        Employee emp = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        String fileUrl = cloudinaryService.uploadFile(file);

        Document document = Document.builder()
                .organization(org)
                .employee(emp)
                .name(name)
                .filename(filename)
                .fileType(mimeType)
                .url(fileUrl)
                .verificationStatus(Document.VerificationStatus.PENDING)
                .build();

        return documentRepository.save(document);
    }


    @Transactional
    public Document saveOrganizationDocument(Organization org, String name, String filename, String mimeType, String url) {
        Document document = Document.builder()
                .organization(org)
                .name(name)
                .filename(filename)
                .fileType(mimeType)
                .url(url)
                .verificationStatus(Document.VerificationStatus.PENDING)
                .build();

        Document saved = documentRepository.save(document);

        // Acknowledge org admin
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendOrgDocReceived(org.getAdminUser().getEmail(), org.getName(), name);
        }
        // Notify bank admin inbox if configured
        if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
            emailService.sendOrgDocPendingReview(bankAdminInbox, org.getName(), name);
        }

        return saved;
    }
    
    @Transactional
    public void verifyEmployeeDocument(Long id, boolean approve, String rejectionReason, String reviewer) {
        Document document = getDocumentById(id);
        if (approve) {
            document.setVerificationStatus(Document.VerificationStatus.VERIFIED);
            document.setRejectionReason(null);
        } else {
            document.setVerificationStatus(Document.VerificationStatus.REJECTED);
            document.setRejectionReason(rejectionReason);
        }
        document.setReviewer(reviewer);
        documentRepository.save(document);

        // Employee doc decision emails (existing/new behavior)
        if (document.getEmployee() != null) {
            String toEmail = document.getEmployee().getEmail();
            String employeeName = document.getEmployee().getFullName();
            String docName = document.getName();
            String orgName = document.getOrganization() != null ? document.getOrganization().getName() : "";

            if (toEmail != null && !toEmail.isBlank()) {
                if (approve) emailService.sendEmployeeDocumentApproved(toEmail, employeeName, docName, orgName);
                else emailService.sendEmployeeDocumentRejected(toEmail, employeeName, docName, orgName, rejectionReason);
            }
        }
    }


    @Transactional
    public Document saveDocument(Organization org, Employee emp, String name, String filename, String mimeType, String url) {
        Document document = Document.builder()
                .organization(org)
                .employee(emp)
                .name(name)
                .filename(filename)
                .fileType(mimeType)
                .url(url)
                .verificationStatus(Document.VerificationStatus.PENDING)
                .build();

        Document saved = documentRepository.save(document);

        // If employee doc: acknowledge employee and notify org admin
        if (emp != null) {
            if (emp.getEmail() != null) {
                emailService.sendEmpDocReceived(emp.getEmail(), emp.getFullName(), name, org != null ? org.getName() : "");
            }
            if (org != null && org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendEmpDocPendingReview(org.getAdminUser().getEmail(),
                        org.getName(), emp.getFullName(), emp.getEmployeeCode(), name);
            }
        } else if (org != null) {
            // If this path is used to save org-docs, mirror org upload notifications
            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendOrgDocReceived(org.getAdminUser().getEmail(), org.getName(), name);
            }
            if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
                emailService.sendOrgDocPendingReview(bankAdminInbox, org.getName(), name);
            }
        }

        return saved;
    }

    public List<Document> getDocumentsForVerificationPending() {
        return documentRepository.findByVerificationStatus(Document.VerificationStatus.PENDING);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    @Transactional
    public void verifyDocument(Long id, boolean approve, String rejectionReason, String reviewer) {
        Document document = getDocumentById(id);
        if (approve) {
            document.setVerificationStatus(Document.VerificationStatus.VERIFIED);
            document.setRejectionReason(null);
        } else {
            document.setVerificationStatus(Document.VerificationStatus.REJECTED);
            document.setRejectionReason(rejectionReason);
        }
        document.setReviewer(reviewer);
        documentRepository.save(document);

        // Organization doc decision emails (existing behavior)
        if (document.getOrganization() != null && document.getEmployee() == null) {
            String orgName = document.getOrganization().getName();
            String toEmail = document.getOrganization().getAdminUser() != null
                    ? document.getOrganization().getAdminUser().getEmail() : null;
            if (toEmail != null && !toEmail.isBlank()) {
                if (approve) emailService.sendOrganizationDocumentApproved(toEmail, orgName, document.getName());
                else emailService.sendOrganizationDocumentRejected(toEmail, orgName, document.getName(), rejectionReason);
            }
        }
    }
    
    public AttachmentResponse storeEmployeeConcernAttachment(Long orgId, Long employeeId, MultipartFile file) {
        try {
            String url = cloudinaryService.uploadFile(file); // returns secure_url
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            long size = file.getSize();

            return new AttachmentResponse(url, contentType, size, Instant.now());
        } catch (Exception e) {
            throw new BusinessRuleException("Upload failed: " + e.getMessage());
        }
    }
}
