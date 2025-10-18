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
import com.aurionpro.service.BankAccountService;
import com.aurionpro.service.DocumentService;
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
    private final DocumentService documentService;
    private final BankAccountService bankAccountService;

    // ✅ View own profile
    @GetMapping("/employee/self")
    public ResponseEntity<EmployeeProfileDTO> getSelfProfile(Authentication authentication) {
        String usernameOrEmail = authentication.getName();
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Employee employee = employeeRepository.findByUserAccountId(user.getId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Fetch statuses from your Document and BankAccount entities or service
        String kycStatus = documentService.getStatusByEmployee(employee);
        String bankStatus = bankAccountService.getStatusByEmployee(employee);

        EmployeeProfileDTO dto = EmployeeProfileDTO.builder()
            .id(employee.getId())
            .organizationId(employee.getOrganization() != null ? employee.getOrganization().getId() : null)
            .fullName(employee.getFullName())
            .email(employee.getEmail())
            .employeeCode(employee.getEmployeeCode())
            .designation(employee.getDesignation())
            .department(employee.getDepartment())
            .dateOfJoining(employee.getDateOfJoining() != null ? employee.getDateOfJoining().format(DateTimeFormatter.ISO_DATE) : null)
            .salaryTemplateId(employee.getSalaryTemplate() != null ? employee.getSalaryTemplate().getId() : null)
            .salaryTemplateName(employee.getSalaryTemplate() != null ? employee.getSalaryTemplate().getTemplateName() : null)
            .kycDocumentStatus(kycStatus)
            .bankAccountStatus(bankStatus)
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
    public ResponseEntity<String> changePassword(
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

        return ResponseEntity.ok("Password Updated successfully");
    }
}
