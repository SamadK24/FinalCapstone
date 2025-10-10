package com.aurionpro.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.entity.Document;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
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
        return documentRepository.save(document);
    }
    public List<Document> getEmployeeDocumentsByOrganizationAndVerificationStatus(Long orgId, Document.VerificationStatus status) {
        return documentRepository.findByOrganizationIdAndVerificationStatus(orgId, status);
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
        return documentRepository.save(document);
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
    }
}
