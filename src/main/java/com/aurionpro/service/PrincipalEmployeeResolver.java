package com.aurionpro.service;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrincipalEmployeeResolver {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Long resolveEmployeeId(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            throw new IllegalArgumentException("Authenticated user required");
        }

        String username = principal.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Verify principal has ROLE_EMPLOYEE
        Set<String> authorities = principal.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());
        if (!authorities.contains("ROLE_EMPLOYEE")) {
            throw new IllegalArgumentException("Access restricted to employee accounts");
        }

        Employee emp = employeeRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee record not found for user: " + username));

        return emp.getId();
    }
}

