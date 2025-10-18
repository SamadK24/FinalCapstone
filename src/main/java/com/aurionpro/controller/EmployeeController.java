package com.aurionpro.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.dtos.EmployeeProfileDTO;
import com.aurionpro.dtos.EmployeeResponseDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/{orgId}/employee")
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<String> addEmployee(@PathVariable Long orgId, @Valid @RequestBody EmployeeCreationDTO dto) {
        employeeService.addEmployee(orgId, dto);
        return ResponseEntity.ok("Employee added successfully");
    }
//    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
//    @GetMapping
//    public ResponseEntity<List<EmployeeProfileDTO>> getEmployees(@PathVariable Long orgId) {
//        List<EmployeeProfileDTO> employees = employeeService.getEmployeesByOrganization(orgId);
//        return ResponseEntity.ok(employees);
//    }
    
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN') and @securityService.isOrgAdmin(#orgId, authentication.name)")
    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeResponseDTO>> listEmployees(
            @PathVariable Long orgId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        
        Employee.Status statusEnum = status != null ? Employee.Status.valueOf(status.toUpperCase()) : null;
        Page<EmployeeResponseDTO> employees = employeeService.listEmployeesForOrganization(orgId, statusEnum, search, pageable);
        return ResponseEntity.ok(employees);
    }


}
