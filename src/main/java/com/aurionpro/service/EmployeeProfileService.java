package com.aurionpro.service;

import com.aurionpro.dtos.UpdateEmployeeProfileRequest;
import com.aurionpro.entity.Employee;

public interface EmployeeProfileService {

    /**
     * Load employee by organization and employee ID.
     */
    Employee loadEmployee(Long orgId, Long employeeId);

    /**
     * Update employee profile fields (phone, altEmail, address).
     */
    void updateProfile(Long orgId, Long employeeId, Long actorUserId, UpdateEmployeeProfileRequest req);

    /**
     * Change employee password with validation.
     */
    void changePassword(Long orgId, Long employeeId, Long actorUserId,
                        String currentPassword, String newPassword, String confirm);
}
