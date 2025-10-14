package com.aurionpro.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ChangePasswordRequest;
import com.aurionpro.dtos.EmployeeProfileDTO;
import com.aurionpro.dtos.UpdateEmployeeProfileRequest;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.User;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.security.CustomUserDetails;
import com.aurionpro.service.EmployeeProfileService;
import com.aurionpro.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class EmployeeProfileController {

    private final EmployeeProfileService employeeProfileService;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeService employeeService;

    // ✅ View own profile
    @GetMapping("/employee/self")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<EmployeeProfileDTO> getSelfProfile(Authentication authentication) {
        String usernameOrEmail = authentication.getName();  // username or email

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + usernameOrEmail));

        Long userId = user.getId();

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

    // ✅ Update profile (only own)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/organization/{orgId}/employees/{employeeId}/profile")
    public ResponseEntity<Void> update(
            @PathVariable Long orgId,
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeProfileRequest req,
            Authentication auth) {

        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        Employee emp = employeeProfileService.loadEmployee(orgId, employeeId);

        if (emp.getUserAccount() == null || !emp.getUserAccount().getId().equals(cud.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Not allowed to update another employee's profile");
        }

        employeeProfileService.updateProfile(orgId, employeeId, cud.getId(), req);
        return ResponseEntity.ok().build();
    }

    // ✅ Change password (only own)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/organization/{orgId}/employees/{employeeId}/profile/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long orgId,
            @PathVariable Long employeeId,
            @Valid @RequestBody ChangePasswordRequest req,
            Authentication auth) {

        CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
        Employee emp = employeeProfileService.loadEmployee(orgId, employeeId);

        if (emp.getUserAccount() == null || !emp.getUserAccount().getId().equals(cud.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Not allowed to change another employee's password");
        }

        employeeProfileService.changePassword(orgId, employeeId, cud.getId(),
                req.getCurrentPassword(), req.getNewPassword(), req.getConfirmNewPassword());

        return ResponseEntity.ok().build();
    }
}
