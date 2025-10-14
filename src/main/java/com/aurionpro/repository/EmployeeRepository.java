package com.aurionpro.repository;

import com.aurionpro.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByUserAccountId(Long userId);

    boolean existsByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Long> findIdByOrganizationIdAndEmployeeCode(Long organizationId, String employeeCode);

    Optional<Employee> findByIdAndOrganization_Id(Long id, Long orgId);

    Optional<Employee> findByIdAndOrganization_IdAndStatus(Long id, Long orgId, Employee.Status status);

    boolean existsByEmployeeCodeAndOrganization_IdAndStatus(String employeeCode, Long orgId, Employee.Status status);
}

