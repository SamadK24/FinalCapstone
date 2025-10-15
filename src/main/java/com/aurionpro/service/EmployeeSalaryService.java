package com.aurionpro.service;

import com.aurionpro.dtos.EmployeeSalaryOverrideDTO;

public interface EmployeeSalaryService {

    /**
     * Assign a salary template to an employee.
     */
    void assignTemplateToEmployee(Long employeeId, Long templateId);

    /**
     * Override specific salary components for an employee.
     */
    void overrideEmployeeSalary(EmployeeSalaryOverrideDTO overrideDTO);
}
