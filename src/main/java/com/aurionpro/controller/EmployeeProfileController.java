package com.aurionpro.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.EmployeeProfileDTO;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.User;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.EmployeeService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Validated
public class EmployeeProfileController {
	
	 private final EmployeeRepository employeeRepository;
	 private final UserRepository userRepository;
	    private final EmployeeService employeeService;
	    @GetMapping("/self")
	    public ResponseEntity<EmployeeProfileDTO> getSelfProfile(Authentication authentication) {
	        String usernameOrEmail = authentication.getName();  // this is the username or email
	        
	        // Use UserRepository to get User entity first
	        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
	                .orElseThrow(() -> new RuntimeException("User not found: " + usernameOrEmail));

	        Long userId = user.getId();
	        
	        // Find Employee by userId
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
