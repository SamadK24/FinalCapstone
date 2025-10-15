package com.aurionpro.service.impl;

import java.time.Instant;
import java.util.Comparator;
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
import com.aurionpro.service.CloudinaryService;
import com.aurionpro.service.DocumentService;
import com.aurionpro.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final CloudinaryService cloudinaryService;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;

    @Override
    @Transactional
    public Document saveEmployeeDocument(Long orgId, Long empId, String name, String filename, String mimeType, MultipartFile file) throws java.io.IOException {
        var org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        var emp = employeeRepository.findById(empId)
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

    @Override
    public Document getLatestDocumentForOrganization(Long orgId) {
        List<Document> docs = documentRepository.findByOrganizationId(orgId);
        return docs.stream()
            .sorted(Comparator.comparing(Document::getId).reversed())
            .findFirst()
            .orElse(null);
    }

    @Override
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

        // Notify org admin
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendOrgDocReceived(org.getAdminUser().getEmail(), org.getName(), name);
        }
        // Notify bank admin inbox
        if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
            emailService.sendOrgDocPendingReview(bankAdminInbox, org.getName(), name);
        }
        if (org.getStatus() == Organization.Status.REJECTED) {
            org.setStatus(Organization.Status.PENDING);
            organizationRepository.save(org);
        }

        return saved;
    }

    @Override
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

        // Notify employee
        if (document.getEmployee() != null) {
            var toEmail = document.getEmployee().getEmail();
            if (toEmail != null && !toEmail.isBlank()) {
                if (approve) emailService.sendEmployeeDocumentApproved(toEmail, document.getEmployee().getFullName(), document.getName(), document.getOrganization().getName());
                else emailService.sendEmployeeDocumentRejected(toEmail, document.getEmployee().getFullName(), document.getName(), document.getOrganization().getName(), rejectionReason);
            }
        }
    }

    @Override
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

        // Notify parties
        if (emp != null && emp.getEmail() != null) {
            emailService.sendEmpDocReceived(emp.getEmail(), emp.getFullName(), name, org != null ? org.getName() : "");
        }
        if (org != null && org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendEmpDocPendingReview(org.getAdminUser().getEmail(), org.getName(), emp != null ? emp.getFullName() : "", emp != null ? emp.getEmployeeCode() : "", name);
        }

        return saved;
    }

    @Override
    public List<Document> getDocumentsForVerificationPending() {
        return documentRepository.findByVerificationStatus(Document.VerificationStatus.PENDING);
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    @Override
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

        Organization org = document.getOrganization();
        if (org != null) {
            org.setStatus(approve ? Organization.Status.APPROVED : Organization.Status.REJECTED);
            organizationRepository.save(org);
        }

        // Notify org admin
        if (document.getOrganization() != null && document.getEmployee() == null) {
            var toEmail = document.getOrganization().getAdminUser() != null ? document.getOrganization().getAdminUser().getEmail() : null;
            if (toEmail != null && !toEmail.isBlank()) {
                if (approve) emailService.sendOrganizationDocumentApproved(toEmail, org.getName(), document.getName());
                else emailService.sendOrganizationDocumentRejected(toEmail, org.getName(), document.getName(), rejectionReason);
            }
        }
    }

    @Override
    public List<Document> getDocumentsPendingByOrganization(Long orgId) {
        return documentRepository.findByOrganizationIdAndVerificationStatus(orgId, Document.VerificationStatus.PENDING);
    }

    @Override
    public AttachmentResponse storeEmployeeConcernAttachment(Long orgId, Long employeeId, MultipartFile file) {
        try {
            String url = cloudinaryService.uploadFile(file);
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            long size = file.getSize();
            return new AttachmentResponse(url, contentType, size, Instant.now());
        } catch (Exception e) {
            throw new BusinessRuleException("Upload failed: " + e.getMessage());
        }
    }
}

