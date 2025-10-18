package com.aurionpro.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.BankAccountDTO;
import com.aurionpro.dtos.EmployeeCreationDTO;
import com.aurionpro.dtos.EmployeeProfileDTO;
import com.aurionpro.dtos.SalaryTemplateDTO;
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
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.EmployeeService;

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
//chnages by durgesh
    @Override
    public List<EmployeeProfileDTO> getEmployeesByOrganization(Long orgId) {
        List<Employee> employees = employeeRepository.findByOrganizationId(orgId);

        return employees.stream().map(employee -> {
            EmployeeProfileDTO dto = modelMapper.map(employee, EmployeeProfileDTO.class);
            
            // Map salary template
            if (employee.getSalaryTemplate() != null) {
                dto.setSalaryTemplateId(employee.getSalaryTemplate().getId());
                dto.setSalaryTemplateName(employee.getSalaryTemplate().getTemplateName());
                
                // Map full salary template for frontend calculations
                SalaryTemplateDTO templateDTO = new SalaryTemplateDTO();
                templateDTO.setId(employee.getSalaryTemplate().getId());
                templateDTO.setTemplateName(employee.getSalaryTemplate().getTemplateName());
                templateDTO.setBasicSalary(employee.getSalaryTemplate().getBasicSalary());
                templateDTO.setHra(employee.getSalaryTemplate().getHra());
                templateDTO.setAllowances(employee.getSalaryTemplate().getAllowances());
                templateDTO.setDeductions(employee.getSalaryTemplate().getDeductions());
                dto.setSalaryTemplate(templateDTO);
            }
            
            // Map date of joining
         // ✅ CORRECT
            if (employee.getDateOfJoining() != null) {
                dto.setDateOfJoining(employee.getDateOfJoining().format(DateTimeFormatter.ISO_DATE));
            }

            
            // Map employee status
            if (employee.getStatus() != null) {
                dto.setStatus(employee.getStatus().toString());
            }
            
            // ✅ Map bank accounts - SAFE VERSION
            if (employee.getBankAccounts() != null && !employee.getBankAccounts().isEmpty()) {
                List<BankAccountDTO> bankAccountDTOs = employee.getBankAccounts().stream()
                    .map(ba -> {
                        BankAccountDTO baDto = new BankAccountDTO();
                        baDto.setId(ba.getId());
                        baDto.setAccountNumber(ba.getAccountNumber());
                        baDto.setBankName(ba.getBankName());
                        baDto.setIfscCode(ba.getIfscCode());
                        
                        if (ba.getKycStatus() != null) {
                            baDto.setKycStatus(ba.getKycStatus().toString());
                        }
                        
                        if (ba.getBalance() != null) {
                            baDto.setBalance(ba.getBalance());
                        }
                        
                        return baDto;
                    })
                    .collect(Collectors.toList());
                dto.setBankAccounts(bankAccountDTOs);
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
}

