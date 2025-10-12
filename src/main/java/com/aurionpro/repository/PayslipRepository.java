package com.aurionpro.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aurionpro.entity.Payslip;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    List<Payslip> findByEmployeeId(Long employeeId);
    List<Payslip> findByEmployeeIdAndSalaryMonth(Long employeeId, LocalDate salaryMonth);
    Optional<Payslip> findByLineId(Long lineId);
    boolean existsByIdAndOrganizationIdAndEmployeeId(Long id, Long organizationId, Long employeeId);
}

