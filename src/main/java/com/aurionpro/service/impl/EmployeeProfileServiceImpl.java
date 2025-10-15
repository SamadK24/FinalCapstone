package com.aurionpro.service.impl;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.UpdateEmployeeProfileRequest;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.EmployeeProfileService;
import com.aurionpro.service.PasswordPolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Employee loadEmployee(Long orgId, Long employeeId) {
        return employeeRepository.findByIdAndOrganization_Id(employeeId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Override
    @Transactional
    public void updateProfile(Long orgId, Long employeeId, Long actorUserId, UpdateEmployeeProfileRequest req) {
        Employee emp = loadEmployee(orgId, employeeId);

        if (emp.getUserAccount() == null || !emp.getUserAccount().getId().equals(actorUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Not allowed to update another employee's profile");
        }

        if (req.getPhone() != null) emp.setPhone(req.getPhone());
        if (req.getAltEmail() != null) emp.setAltEmail(req.getAltEmail());
        if (req.getAddress() != null) emp.setAddress(req.getAddress());
        employeeRepository.save(emp);
    }

    @Override
    @Transactional
    public void changePassword(Long orgId, Long employeeId, Long actorUserId,
                               String currentPassword, String newPassword, String confirm) {
        if (!Objects.equals(newPassword, confirm)) {
            throw new BusinessRuleException("Passwords do not match");
        }

        Employee emp = loadEmployee(orgId, employeeId);

        if (emp.getUserAccount() == null || !emp.getUserAccount().getId().equals(actorUserId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Not allowed to change another employee's password");
        }

        User user = emp.getUserAccount();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        passwordPolicy.validateNewPassword(currentPassword, newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

