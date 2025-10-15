package com.aurionpro.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.dtos.EmployeeProfileDTO;
import com.aurionpro.entity.Employee;

public interface EmployeeService {

    /**
     * Add a new employee to an organization
     */
    void addEmployee(Long orgId, EmployeeCreationDTO dto);

    /**
     * Get all employees for an organization
     */
    List<EmployeeProfileDTO> getEmployeesByOrganization(Long orgId);

    /**
     * Load employee by ID
     */
    Employee getEmployeeById(Long employeeId);
    

    /**
     * Ensure the employee belongs to the given organization
     */
    void assertEmployeeInOrg(Long employeeId, Long orgId);

    // Optional: resolve employeeId by code
    // Long resolveEmployeeId(Long orgId, String employeeCode);
}