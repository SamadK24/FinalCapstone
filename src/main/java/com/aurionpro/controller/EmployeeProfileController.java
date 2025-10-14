package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ChangePasswordRequest;
import com.aurionpro.dtos.UpdateEmployeeProfileRequest;
import com.aurionpro.entity.Employee;
import com.aurionpro.security.CustomUserDetails;
import com.aurionpro.service.EmployeeProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/employees/{employeeId}/profile")
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping
    public ResponseEntity<Void> update(
        @PathVariable Long orgId,
        @PathVariable Long employeeId,
        @Valid @RequestBody UpdateEmployeeProfileRequest req,
        Authentication auth
    ) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();

        // Load the employee for this path to validate ownership
        Employee emp = employeeProfileService.loadEmployee(orgId, employeeId);
        if (emp.getUserAccount() == null || !emp.getUserAccount().getId().equals(cud.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed to update another employee's profile");
        }

        employeeProfileService.updateProfile(orgId, employeeId, cud.getId(), req);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long orgId,
        @PathVariable Long employeeId,
        @Valid @RequestBody ChangePasswordRequest req,
        Authentication auth
    ) {
        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();

        // Load the employee for this path to validate ownership
        Employee emp = employeeProfileService.loadEmployee(orgId, employeeId);
        if (emp.getUserAccount() == null || !emp.getUserAccount().getId().equals(cud.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed to change another employee's password");
        }

        employeeProfileService.changePassword(orgId, employeeId, cud.getId(),
            req.getCurrentPassword(), req.getNewPassword(), req.getConfirmNewPassword());
        return ResponseEntity.ok().build();
    }

}

