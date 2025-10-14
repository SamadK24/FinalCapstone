package com.aurionpro.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.EmployeeResponseDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.service.EmployeeAdminService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/employees")
public class OrgEmployeesController {

    private final EmployeeAdminService employeeAdminService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> list(
        @PathVariable Long orgId,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Employee.Status status,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        var result = employeeAdminService.list(orgId, search, status, pageable);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> get(
        @PathVariable Long orgId,
        @PathVariable Long employeeId
    ) {
        return ResponseEntity.ok(employeeAdminService.get(orgId, employeeId));
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> softDelete(
        @PathVariable Long orgId,
        @PathVariable Long employeeId
    ) {
        employeeAdminService.softDelete(orgId, employeeId);
        return ResponseEntity.noContent().build();
    }
}

