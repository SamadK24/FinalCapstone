package com.aurionpro.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.EmployeeResponseDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.service.EmployeeAdminService;
import com.aurionpro.specs.EmployeeSpecs;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeAdminServiceImpl implements EmployeeAdminService {

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> list(Long orgId, String search, Employee.Status status, Pageable pageable) {
        var spec = EmployeeSpecs.forOrgWithFilters(orgId, status, search);
        return employeeRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDTO get(Long orgId, Long employeeId) {
        Employee emp = employeeRepository.findByIdAndOrganization_Id(employeeId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        return toDto(emp);
    }

    @Override
    @Transactional
    public void softDelete(Long orgId, Long employeeId) {
        Employee emp = employeeRepository.findByIdAndOrganization_Id(employeeId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        if (emp.getStatus() == Employee.Status.INACTIVE) return;
        emp.setStatus(Employee.Status.INACTIVE);
        employeeRepository.save(emp);
    }

    private EmployeeResponseDTO toDto(Employee e) {
        return EmployeeResponseDTO.builder()
                .id(e.getId())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .employeeCode(e.getEmployeeCode())
                .dateOfJoining(e.getDateOfJoining())
                .designation(e.getDesignation())
                .department(e.getDepartment())
                .phone(e.getPhone())
                .altEmail(e.getAltEmail())
                .address(e.getAddress())
                .status(e.getStatus())
                .build();
    }
}
