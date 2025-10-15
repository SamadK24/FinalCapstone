package com.aurionpro.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aurionpro.dtos.EmployeeResponseDTO;
import com.aurionpro.entity.Employee;

public interface EmployeeAdminService {

    Page<EmployeeResponseDTO> list(Long orgId, String search, Employee.Status status, Pageable pageable);

    EmployeeResponseDTO get(Long orgId, Long employeeId);

    void softDelete(Long orgId, Long employeeId);
}
