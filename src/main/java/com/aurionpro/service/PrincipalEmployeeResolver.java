package com.aurionpro.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface PrincipalEmployeeResolver {

    /**
     * Resolve the Employee ID for a given authenticated principal.
     * Throws exception if principal is invalid or not an employee.
     */
    Long resolveEmployeeId(UserDetails principal);
}
