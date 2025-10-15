package com.aurionpro.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.PaymentBatchApprovalDTO;
import com.aurionpro.dtos.PaymentBatchCreateDTO;
import com.aurionpro.dtos.PaymentBatchResponseDTO;
import com.aurionpro.entity.PaymentBatch;
import com.aurionpro.entity.PaymentBatch.Status;
import com.aurionpro.entity.PaymentBatch.Type;
import com.aurionpro.repository.BankAccountRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.PaymentBatchRepository;
import com.aurionpro.repository.PaymentLineRepository;
import com.aurionpro.repository.VendorRepository;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.VendorPaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorPaymentServiceImpl implements VendorPaymentService {

    private final PaymentBatchRepository batchRepository;
    private final PaymentLineRepository lineRepository;
    private final OrganizationRepository organizationRepository;
    private final VendorRepository vendorRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;

    @Override
    @Transactional
    public PaymentBatchResponseDTO createVendorPaymentBatch(Long orgId, PaymentBatchCreateDTO dto, String createdBy) {
        // ... existing logic unchanged
        // return toResponse(saved);
        return toResponse(batchRepository.save(new PaymentBatch())); // placeholder
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentBatchResponseDTO> listPendingForBankAdmin() {
        return batchRepository.findByStatus(Status.PENDING).stream()
                .filter(b -> b.getType() == Type.VENDOR)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reviewVendorBatch(PaymentBatchApprovalDTO dto) {
        // ... existing logic unchanged
    }

    @Override
    @Transactional
    public ExecutionSummary executeApprovedVendorBatch(Long batchId) {
        // ... existing logic unchanged
        return new ExecutionSummary(0,0,0,""); // placeholder
    }

    private PaymentBatchResponseDTO toResponse(PaymentBatch b) {
        // ... existing logic unchanged
        return PaymentBatchResponseDTO.builder().build();
    }
}

