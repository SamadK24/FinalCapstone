package com.aurionpro.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.AttachmentResponse;
import com.aurionpro.dtos.DocumentResponseDTO;
import com.aurionpro.entity.Document;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;

public interface DocumentService {

    Document saveEmployeeDocument(Long orgId, Long empId, String name, String filename, String mimeType, MultipartFile file) throws java.io.IOException;

    Document getLatestDocumentForOrganization(Long orgId);

    Document saveOrganizationDocument(Organization org, String name, String filename, String mimeType, String url);

    void verifyEmployeeDocument(Long id, boolean approve, String rejectionReason, String reviewer);

    Document saveDocument(Organization org, Employee emp, String name, String filename, String mimeType, String url);

    List<Document> getDocumentsForVerificationPending();

    Document getDocumentById(Long id);

    void verifyDocument(Long id, boolean approve, String rejectionReason, String reviewer);
    List<Document> getDocumentsPendingByOrganization(Long orgId);
    String getStatusByEmployee(Employee employee);

    AttachmentResponse storeEmployeeConcernAttachment(Long orgId, Long employeeId, MultipartFile file);
    
    Page<DocumentResponseDTO> getPendingDocuments(Pageable pageable);
    Page<DocumentResponseDTO> getPendingDocumentsByOrganization(Long orgId, Pageable pageable);

}