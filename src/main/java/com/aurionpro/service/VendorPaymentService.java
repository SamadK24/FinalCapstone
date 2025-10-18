package com.aurionpro.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aurionpro.dtos.PaymentBatchApprovalDTO;
import com.aurionpro.dtos.PaymentBatchCreateDTO;
import com.aurionpro.dtos.PaymentBatchResponseDTO;

public interface VendorPaymentService {

    PaymentBatchResponseDTO createVendorPaymentBatch(Long orgId, PaymentBatchCreateDTO dto, String createdBy);

    Page<PaymentBatchResponseDTO> listPendingForBankAdmin(Pageable pageable);


    void reviewVendorBatch(PaymentBatchApprovalDTO dto);

    ExecutionSummary executeApprovedVendorBatch(Long batchId);

    class ExecutionSummary {
        private final int totalLines;
        private final int paidLines;
        private final int skippedLines;
        private final String batchStatus;

        public ExecutionSummary(int totalLines, int paidLines, int skippedLines, String batchStatus) {
            this.totalLines = totalLines;
            this.paidLines = paidLines;
            this.skippedLines = skippedLines;
            this.batchStatus = batchStatus;
        }

        public int getTotalLines() { return totalLines; }
        public int getPaidLines() { return paidLines; }
        public int getSkippedLines() { return skippedLines; }
        public String getBatchStatus() { return batchStatus; }
    }
}
