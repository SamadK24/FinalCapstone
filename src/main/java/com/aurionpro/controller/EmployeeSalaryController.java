package com.aurionpro.controller;

import com.aurionpro.dtos.EmployeeSalaryOverrideDTO;
import com.aurionpro.service.EmployeeSalaryService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Validated
public class EmployeeSalaryController {

    private final EmployeeSalaryService employeeSalaryService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/assign-template/{employeeId}/{templateId}")
    public ResponseEntity<?> assignTemplate(@PathVariable Long employeeId,
                                            @PathVariable Long templateId) {
        employeeSalaryService.assignTemplateToEmployee(employeeId, templateId);
        // Always return JSON object, not a raw string
        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Salary template assigned successfully"));
    }


    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/override-salary")
    public ResponseEntity<String> overrideSalary(@Valid @RequestBody EmployeeSalaryOverrideDTO overrideDTO) {
        employeeSalaryService.overrideEmployeeSalary(overrideDTO);
        return ResponseEntity.ok("Employee salary override updated successfully");
    }
}
