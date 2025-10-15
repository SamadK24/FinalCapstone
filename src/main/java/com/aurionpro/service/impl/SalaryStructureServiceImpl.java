package com.aurionpro.service.impl;

import com.aurionpro.dtos.SalaryStructureRequestDTO;
import com.aurionpro.dtos.SalaryStructureResponseDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.SalaryStructure;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.SalaryStructureRepository;
import com.aurionpro.service.SalaryStructureService;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalaryStructureServiceImpl implements SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SalaryStructureResponseDTO createOrUpdateSalaryStructure(Long employeeId, SalaryStructureRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        SalaryStructure salaryStructure = salaryStructureRepository.findByEmployeeId(employeeId)
                .orElse(new SalaryStructure());

        salaryStructure.setBasicSalary(requestDTO.getBasicSalary());
        salaryStructure.setHra(requestDTO.getHra());
        salaryStructure.setAllowances(requestDTO.getAllowances());
        salaryStructure.setDeductions(requestDTO.getDeductions());
        salaryStructure.setEmployee(employee);

        SalaryStructure saved = salaryStructureRepository.save(salaryStructure);
        return modelMapper.map(saved, SalaryStructureResponseDTO.class);
    }

    @Override
    public SalaryStructureResponseDTO getSalaryStructureByEmployeeId(Long employeeId) {
        SalaryStructure salaryStructure = salaryStructureRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary Structure not found"));

        return modelMapper.map(salaryStructure, SalaryStructureResponseDTO.class);
    }
}

