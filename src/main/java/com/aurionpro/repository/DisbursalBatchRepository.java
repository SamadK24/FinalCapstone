package com.aurionpro.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.DisbursalBatch;

@Repository
public interface DisbursalBatchRepository extends JpaRepository<DisbursalBatch, Long> {

    // ✅ Eager fetch for detailed data view (no pagination)
    @Query("SELECT DISTINCT b FROM DisbursalBatch b " +
           "LEFT JOIN FETCH b.organization " +
           "LEFT JOIN FETCH b.lines l " +
           "LEFT JOIN FETCH l.employee " +
           "WHERE b.status = :status")
    List<DisbursalBatch> findByStatus(@Param("status") DisbursalBatch.Status status);

    // ✅ Pageable version for dashboards or list views
    Page<DisbursalBatch> findByStatus(DisbursalBatch.Status status, Pageable pageable);

    List<DisbursalBatch> findByOrganizationIdAndSalaryMonth(Long orgId, LocalDate salaryMonth);

    boolean existsByOrganizationIdAndSalaryMonth(Long orgId, LocalDate salaryMonth);
}
