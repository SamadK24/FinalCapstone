package com.aurionpro.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.SalaryDisbursalRequest;
import com.aurionpro.entity.SalaryDisbursalRequest.Status;

@Repository
public interface SalaryDisbursalRequestRepository extends JpaRepository<SalaryDisbursalRequest, Long> {
    List<SalaryDisbursalRequest> findByOrganizationIdAndStatus(Long orgId, Status status);
    Page<SalaryDisbursalRequest> findByStatus(SalaryDisbursalRequest.Status status, Pageable pageable);
    
    // Prevent duplicate requests for same employee and month
    boolean existsByEmployeeIdAndSalaryMonth(Long employeeId, LocalDate salaryMonth);
    
    // Find existing request for validation
    Optional<SalaryDisbursalRequest> findByEmployeeIdAndSalaryMonth(Long employeeId, LocalDate salaryMonth);
}

