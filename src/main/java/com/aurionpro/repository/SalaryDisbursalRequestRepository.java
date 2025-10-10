package com.aurionpro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.SalaryDisbursalRequest;
import com.aurionpro.entity.SalaryDisbursalRequest.Status;

@Repository
public interface SalaryDisbursalRequestRepository extends JpaRepository<SalaryDisbursalRequest, Long> {
    List<SalaryDisbursalRequest> findByOrganizationIdAndStatus(Long orgId, Status status);
    List<SalaryDisbursalRequest> findByStatus(Status status);
}
