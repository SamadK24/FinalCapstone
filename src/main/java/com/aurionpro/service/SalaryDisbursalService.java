package com.aurionpro.service;

import com.aurionpro.dtos.SalaryDisbursalApprovalDTO;
import com.aurionpro.dtos.SalaryDisbursalRequestDTO;
import com.aurionpro.dtos.SalaryDisbursalResponseDTO;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalaryDisbursalService {

    /**
     * Create a salary disbursal request for an employee in the organization.
     */
    SalaryDisbursalResponseDTO createSalaryDisbursalRequest(Long orgId, SalaryDisbursalRequestDTO requestDTO);

    /**
     * List all pending salary disbursal requests for bank admins.
     */
    Page<SalaryDisbursalResponseDTO> getPendingRequestsForBankAdmin(Pageable pageable);

    /**
     * Approve or reject a pending salary disbursal request.
     */
    void approveOrRejectRequest(SalaryDisbursalApprovalDTO approvalDTO);
}
