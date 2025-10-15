package com.aurionpro.service.impl;

import com.aurionpro.entity.Employee;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.UserRepository;
import com.aurionpro.service.PrincipalEmployeeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrincipalEmployeeResolverImpl implements PrincipalEmployeeResolver {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public Long resolveEmployeeId(UserDetails principal) {
        if (principal == null || principal.getUsername() == null) {
            throw new IllegalArgumentException("Authenticated user required");
        }

        String username = principal.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

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

