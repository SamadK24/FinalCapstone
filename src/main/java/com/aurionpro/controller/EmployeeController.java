package com.aurionpro.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.dtos.EmployeeProfileDTO;
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
    @GetMapping("/self")
    public ResponseEntity<EmployeeProfileDTO> getSelfProfile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());  // adjust according to your authentication principal

        Employee employee = employeeRepository.findByUserAccountId(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found for user " + userId));

        EmployeeProfileDTO dto = EmployeeProfileDTO.builder()
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .employeeCode(employee.getEmployeeCode())
                .designation(employee.getDesignation())
                .department(employee.getDepartment())
                .dateOfJoining(employee.getDateOfJoining() != null
                        ? employee.getDateOfJoining().format(DateTimeFormatter.ISO_DATE)
                        : null)
                .build();

        return ResponseEntity.ok(dto);
    }
}
