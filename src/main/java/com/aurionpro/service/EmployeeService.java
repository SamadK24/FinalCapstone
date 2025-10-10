package com.aurionpro.service;

import java.util.HashSet;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.Role;
import com.aurionpro.entity.Role.RoleName;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.OrganizationNotApprovedException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.RoleRepository;
import com.aurionpro.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void addEmployee(Long orgId, EmployeeCreationDTO dto) {

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (organization.getStatus() != Organization.Status.APPROVED) {
            throw new OrganizationNotApprovedException("Organization is not approved by Bank Admin yet");
        }

        if (userRepository.existsByUsername(dto.getEmployeeCode()))
            throw new RuntimeException("Employee username (code) already exists");

        if (userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Employee email already exists");

        User employeeUser = new User();
        employeeUser.setUsername(dto.getEmployeeCode());
        employeeUser.setEmail(dto.getEmail());
        // Initial default password to be reset later via invite/email
        employeeUser.setPassword(passwordEncoder.encode("defaultPassword123"));

        Role employeeRole = roleRepository.findByName(RoleName.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Employee Role not found"));

        employeeUser.setRoles(new HashSet<>(java.util.Collections.singletonList(employeeRole)));

        userRepository.save(employeeUser);

        Employee employee = Employee.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .employeeCode(dto.getEmployeeCode())
                .userAccount(employeeUser)
                .organization(organization)
                .build();

        employeeRepository.save(employee);

        // TODO: Send employee invite email with password reset link
    }
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

}
