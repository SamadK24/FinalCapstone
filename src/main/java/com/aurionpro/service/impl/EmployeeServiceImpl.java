package com.aurionpro.service.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.dtos.EmployeeProfileDTO;
import com.aurionpro.dtos.EmployeeResponseDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Employee.Status;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.Role;
import com.aurionpro.entity.Role.RoleName;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.OrganizationNotApprovedException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.RoleRepository;
import com.aurionpro.repository.SalaryTemplateRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.EmployeeService;
import com.aurionpro.specs.EmployeeSpecs;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    @Autowired
    private SalaryTemplateRepository salaryTemplateRepository;
    

    @Override
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

        LocalDate joiningDate = (dto.getDateOfJoining() == null || dto.getDateOfJoining().isEmpty())
                ? LocalDate.now()
                : LocalDate.parse(dto.getDateOfJoining());

        // generate temp password
        String tempPassword = "defaultPassword123"; // you can generate random secure password here

        User employeeUser = new User();
        employeeUser.setUsername(dto.getEmployeeCode());
        employeeUser.setEmail(dto.getEmail());
        employeeUser.setPassword(passwordEncoder.encode(tempPassword));

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
                .department(dto.getDepartment())
                .designation(dto.getDesignation())
                .dateOfJoining(joiningDate)
                .status(Status.ACTIVE)
                .build();

        employeeRepository.save(employee);

        // send welcome email
        emailService.sendEmployeeWelcomeWithCredentials(
                employee.getEmail(),
                employee.getFullName(),
                employeeUser.getUsername(),
                tempPassword
        );
    }

    @Override
    public List<EmployeeProfileDTO> getEmployeesByOrganization(Long orgId) {
        List<Employee> employees = employeeRepository.findByOrganizationId(orgId);

        return employees.stream().map(employee -> {
            EmployeeProfileDTO dto = modelMapper.map(employee, EmployeeProfileDTO.class);
            if (employee.getSalaryTemplate() != null) {
                dto.setSalaryTemplateId(employee.getSalaryTemplate().getId());
                dto.setSalaryTemplateName(employee.getSalaryTemplate().getTemplateName());
            }
            if (employee.getDateOfJoining() != null) {
                dto.setDateOfJoining(employee.getDateOfJoining().toString());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    @Override
    public void assertEmployeeInOrg(Long employeeId, Long orgId) {
        boolean exists = employeeRepository.existsByIdAndOrganizationId(employeeId, orgId);
        if (!exists) {
            throw new ResourceNotFoundException("Employee not found in organization");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDTO> listEmployeesForOrganization(Long orgId, Employee.Status status, String search, Pageable pageable) {
        Specification<Employee> spec = EmployeeSpecs.forOrgWithFilters(orgId, status, search);
        Page<Employee> employees = employeeRepository.findAll(spec, pageable);
        return employees.map(e -> modelMapper.map(e, EmployeeResponseDTO.class));
    }

}

