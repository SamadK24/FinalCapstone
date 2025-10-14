package com.aurionpro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByUserAccountId(Long userId);

    boolean existsByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Long> findIdByOrganizationIdAndEmployeeCode(Long organizationId, String employeeCode);

    Optional<Employee> findByIdAndOrganization_Id(Long id, Long orgId);

    Optional<Employee> findByIdAndOrganization_IdAndStatus(Long id, Long orgId, Employee.Status status);

    boolean existsByEmployeeCodeAndOrganization_IdAndStatus(String employeeCode, Long orgId, Employee.Status status);

    // Added from main branch
    List<Employee> findByOrganizationId(Long orgId);
}
