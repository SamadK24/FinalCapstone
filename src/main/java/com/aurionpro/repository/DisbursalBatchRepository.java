package com.aurionpro.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.DisbursalBatch;

@Repository
public interface DisbursalBatchRepository extends JpaRepository<DisbursalBatch, Long> {
	Page<DisbursalBatch> findByStatus(DisbursalBatch.Status status, Pageable pageable);

    List<DisbursalBatch> findByOrganizationIdAndSalaryMonth(Long orgId, LocalDate salaryMonth);
    boolean existsByOrganizationIdAndSalaryMonth(Long orgId, LocalDate salaryMonth);

}

