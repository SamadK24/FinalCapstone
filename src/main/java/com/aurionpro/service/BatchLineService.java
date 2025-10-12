package com.aurionpro.service;

import org.springframework.stereotype.Service;

import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.DisbursalLineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchLineService {

    private final DisbursalLineRepository disbursalLineRepository;

    // Verifies an employee payroll line belongs to the org and employee
    public void assertLineOwnedBy(Long disbursalLineId, Long orgId, Long employeeId) {
        boolean exists = disbursalLineRepository
                .existsByIdAndBatch_OrganizationIdAndEmployeeId(disbursalLineId, orgId, employeeId);

        if (!exists) {
            throw new ResourceNotFoundException("Disbursal line not found for employee in organization");
        }
    }
}

