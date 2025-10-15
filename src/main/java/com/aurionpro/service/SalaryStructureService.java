package com.aurionpro.service;

import com.aurionpro.dtos.SalaryStructureRequestDTO;
import com.aurionpro.dtos.SalaryStructureResponseDTO;

public interface SalaryStructureService {

    /**
     * Create or update the salary structure for an employee.
     */
    SalaryStructureResponseDTO createOrUpdateSalaryStructure(Long employeeId, SalaryStructureRequestDTO requestDTO);

    /**
     * Retrieve salary structure for a given employee.
     */
    SalaryStructureResponseDTO getSalaryStructureByEmployeeId(Long employeeId);
}
