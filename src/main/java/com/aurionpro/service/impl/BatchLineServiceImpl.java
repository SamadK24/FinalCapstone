package com.aurionpro.service.impl;

import org.springframework.stereotype.Service;

import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.DisbursalLineRepository;
import com.aurionpro.service.BatchLineService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchLineServiceImpl implements BatchLineService {

    private final DisbursalLineRepository disbursalLineRepository;

    @Override
    public void assertLineOwnedBy(Long disbursalLineId, Long orgId, Long employeeId) {
        boolean exists = disbursalLineRepository
                .existsByIdAndBatch_OrganizationIdAndEmployeeId(disbursalLineId, orgId, employeeId);

        if (!exists) {
            throw new ResourceNotFoundException("Disbursal line not found for employee in organization");
        }
    }
}

