package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.SalaryStructureRequestDTO;
import com.aurionpro.dtos.SalaryStructureResponseDTO;
import com.aurionpro.service.SalaryStructureService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employee/{employeeId}/salary-structure")
@RequiredArgsConstructor
@Validated
public class SalaryStructureController {

    private final SalaryStructureService salaryStructureService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or hasRole('HR')")
    @PostMapping
    public ResponseEntity<SalaryStructureResponseDTO> createOrUpdateSalaryStructure(
            @PathVariable Long employeeId,
            @Valid @RequestBody SalaryStructureRequestDTO requestDTO) {
        SalaryStructureResponseDTO responseDTO = salaryStructureService.createOrUpdateSalaryStructure(employeeId, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<SalaryStructureResponseDTO> getSalaryStructure(@PathVariable Long employeeId) {
        SalaryStructureResponseDTO responseDTO = salaryStructureService.getSalaryStructureByEmployeeId(employeeId);
        return ResponseEntity.ok(responseDTO);
    }
}
