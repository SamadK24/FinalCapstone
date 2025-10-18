package com.aurionpro.service;

import java.util.List;

import com.aurionpro.dtos.DisbursalBatchApprovalDTO;
import com.aurionpro.dtos.DisbursalBatchCreateDTO;
import com.aurionpro.dtos.DisbursalBatchResponseDTO;

public interface PayrollBatchService {

    DisbursalBatchResponseDTO createBatch(Long orgId, DisbursalBatchCreateDTO dto, String createdBy);

    List<DisbursalBatchResponseDTO> listPendingBatchesForBankAdmin();

    void reviewBatch(DisbursalBatchApprovalDTO dto);

    ExecutionSummary executeApprovedBatch(Long batchId);

    record ExecutionSummary(int totalLines, int paidLines, int skippedLines, String batchStatus) {}
    
    List<DisbursalBatchResponseDTO> listApprovedBatches();

}
