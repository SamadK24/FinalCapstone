package com.aurionpro.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.EmployeeSalaryOverrideDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.SalaryTemplate;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.SalaryTemplateRepository;
import com.aurionpro.service.EmployeeSalaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeSalaryServiceImpl implements EmployeeSalaryService {

    private final EmployeeRepository employeeRepository;
    private final SalaryTemplateRepository salaryTemplateRepository;

    @Override
    @Transactional
    public void assignTemplateToEmployee(Long employeeId, Long templateId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        SalaryTemplate template = salaryTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary template not found"));

        employee.setSalaryTemplate(template);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void overrideEmployeeSalary(EmployeeSalaryOverrideDTO overrideDTO) {
        Employee employee = employeeRepository.findById(overrideDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (overrideDTO.getOverrideBasicSalary() != null)
            employee.setOverrideBasicSalary(overrideDTO.getOverrideBasicSalary());
        if (overrideDTO.getOverrideHra() != null)
            employee.setOverrideHra(overrideDTO.getOverrideHra());
        if (overrideDTO.getOverrideAllowances() != null)
            employee.setOverrideAllowances(overrideDTO.getOverrideAllowances());
        if (overrideDTO.getOverrideDeductions() != null)
            employee.setOverrideDeductions(overrideDTO.getOverrideDeductions());

        employeeRepository.save(employee);
    }
}

