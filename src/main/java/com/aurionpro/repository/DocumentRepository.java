package com.aurionpro.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.entity.Document;
import com.aurionpro.entity.Document.VerificationStatus;
import com.aurionpro.entity.Employee;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOrganizationId(Long organizationId);
    List<Document> findByEmployeeId(Long employeeId); // Add this method
    List<Document> findByVerificationStatus(Document.VerificationStatus verificationStatus);
    List<Document> findByOrganizationIdIsNotNullAndVerificationStatus(Document.VerificationStatus verificationStatus);
    List<Document> findByOrganizationIdAndEmployeeIsNotNullAndVerificationStatus(Long orgId, Document.VerificationStatus verificationStatus);
    List<Document> findByOrganizationIdAndVerificationStatus(Long organizationId, VerificationStatus status);
    List<Document> findByEmployee(Employee employee);
    Page<Document> findByVerificationStatus(Document.VerificationStatus status, Pageable pageable);
    Page<Document> findByOrganizationIdAndVerificationStatus(Long orgId, Document.VerificationStatus status, Pageable pageable);

}
