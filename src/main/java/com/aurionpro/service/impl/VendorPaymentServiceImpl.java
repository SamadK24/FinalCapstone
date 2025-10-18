package com.aurionpro.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.PaymentBatchApprovalDTO;
import com.aurionpro.dtos.PaymentBatchCreateDTO;
import com.aurionpro.dtos.PaymentBatchResponseDTO;
import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.PaymentBatch;
import com.aurionpro.entity.PaymentLine;
import com.aurionpro.entity.Vendor;
import com.aurionpro.exceptions.ResourceNotFoundException;
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
    private final EmailService emailService;

    @Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;

    @Override
    @Transactional
    public PaymentBatchResponseDTO createVendorPaymentBatch(Long orgId, PaymentBatchCreateDTO dto, String createdBy) {
        // 1. Validate organization exists and is approved
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));
        
        if (org.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization must be approved to create payment batches");
        }
        
        // 2. Validate payment date is provided
        if (dto.getPaymentDate() == null) {
            throw new IllegalArgumentException("Payment date is required");
        }
        
        // 3. Validate lines list
        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new IllegalArgumentException("Payment batch must have at least one payment line");
        }
        
        // 4. Check for duplicate batch for same org, type, and date
        List<PaymentBatch> existing = batchRepository.findByOrganizationIdAndTypeAndPaymentDate(
                orgId, PaymentBatch.Type.VENDOR, dto.getPaymentDate());
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Payment batch already exists for this organization and date: " 
                + dto.getPaymentDate());
        }
        
        // 5. Validate organization has verified payroll account
        bankAccountRepository.findFirstVerifiedOrgAccount(orgId)
                .orElseThrow(() -> new IllegalStateException(
                    "Organization does not have a verified payroll bank account"));
        
        // 6. Validate and create payment lines
        Set<PaymentLine> lines = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> processedVendorIds = new HashSet<>();
        
        for (PaymentBatchCreateDTO.Line lineDto : dto.getLines()) {
            // Validate amount
            if (lineDto.getAmount() == null || lineDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Payment amount must be positive");
            }
            
            // Validate vendor ID
            if (lineDto.getVendorId() == null) {
                throw new IllegalArgumentException("Vendor ID is required for each payment line");
            }
            
            // Check for duplicate vendor in same batch
            if (processedVendorIds.contains(lineDto.getVendorId())) {
                throw new IllegalArgumentException("Duplicate vendor in payment batch: vendorId=" + lineDto.getVendorId());
            }
            processedVendorIds.add(lineDto.getVendorId());
            
            // Get vendor and validate
            Vendor vendor = vendorRepository.findByIdAndOrganizationId(lineDto.getVendorId(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found with ID: " + lineDto.getVendorId() + " in organization: " + orgId));
            
            // Validate vendor status is ACTIVE
            if (vendor.getStatus() != Vendor.Status.ACTIVE) {
                throw new IllegalArgumentException("Vendor must be ACTIVE. VendorId: " + vendor.getId());
            }
            
            // Validate vendor KYC is verified
            if (vendor.getKycStatus() != BankAccount.KYCDocumentVerificationStatus.VERIFIED) {
                throw new IllegalArgumentException("Vendor KYC must be VERIFIED. VendorId: " + vendor.getId() 
                    + ", Current status: " + vendor.getKycStatus());
            }
            
            // Validate vendor has bank details
            if (vendor.getAccountNumber() == null || vendor.getIfscCode() == null) {
                throw new IllegalArgumentException("Vendor missing bank account details. VendorId: " + vendor.getId());
            }
            
            // Create payment line
            PaymentLine line = PaymentLine.builder()
                    .vendor(vendor)
                    .amount(lineDto.getAmount())
                    .status(PaymentLine.Status.QUEUED)
                    .build();
            lines.add(line);
            total = total.add(lineDto.getAmount());
        }
        
        // 7. Create batch
        PaymentBatch batch = PaymentBatch.builder()
                .organization(org)
                .type(PaymentBatch.Type.VENDOR)
                .paymentDate(dto.getPaymentDate())
                .totalAmount(total)
                .status(PaymentBatch.Status.PENDING)
                .createdBy(createdBy)
                .build();

        lines.forEach(l -> l.setBatch(batch));
        batch.setLines(lines);

        PaymentBatch saved = batchRepository.save(batch);

        // 8. Send notifications
        if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
            emailService.sendBatchPendingReview(
                    bankAdminInbox,
                    org.getName(),
                    dto.getPaymentDate().toString(),
                    total.toPlainString(),
                    saved.getId());
        }

//        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
//            emailService.sendVendorBatchAcknowledged(
//                    org.getAdminUser().getEmail(),
//                    org.getName(),
//                    dto.getPaymentDate().toString(),
//                    total.toPlainString(),
//                    saved.getId());
//        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentBatchResponseDTO> listPendingForBankAdmin(Pageable pageable) {
        Page<PaymentBatch> batches = batchRepository.findByStatusAndType(
                PaymentBatch.Status.PENDING, 
                PaymentBatch.Type.VENDOR, 
                pageable);
        return batches.map(this::toResponse);
    }


    @Override
    @Transactional
    public void reviewVendorBatch(PaymentBatchApprovalDTO dto) {
        // 1. Validate batch exists
        PaymentBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment batch not found with ID: " + dto.getBatchId()));

        // 2. Validate batch is vendor type
        if (batch.getType() != PaymentBatch.Type.VENDOR) {
            throw new IllegalStateException("Batch is not a vendor payment batch");
        }

        // 3. Validate batch is in PENDING status
        if (batch.getStatus() != PaymentBatch.Status.PENDING) {
            throw new IllegalStateException("Only PENDING batches can be reviewed. Current status: " 
                + batch.getStatus());
        }

        // 4. Validate approval decision is provided
        if (dto.getApprove() == null) {
            throw new IllegalArgumentException("Approval decision must be provided");
        }

        Organization org = batch.getOrganization();

        // 5. Handle rejection
        if (!Boolean.TRUE.equals(dto.getApprove())) {
            // Rejection reason is mandatory
            if (dto.getRejectionReason() == null || dto.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting batch");
            }
            
            batch.setStatus(PaymentBatch.Status.REJECTED);
            batch.setRejectionReason(dto.getRejectionReason().trim());
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(
                        org.getAdminUser().getEmail(),
                        org.getName(),
                        batch.getPaymentDate().toString(),
                        dto.getRejectionReason(),
                        batch.getId());
            }
            return;
        }

        // 6. Handle approval - check org has verified payroll account
        BankAccount orgAccount = bankAccountRepository.findFirstVerifiedOrgAccount(org.getId())
                .orElseThrow(() -> new IllegalStateException("Organization has no verified payroll account"));

        // 7. Lock account for update
        BankAccount locked = bankAccountRepository.findByIdForUpdate(orgAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll account not found"));

        // 8. Check sufficient balance
        BigDecimal available = locked.getBalance();
        BigDecimal required = batch.getTotalAmount();

        if (available.compareTo(required) < 0) {
            batch.setStatus(PaymentBatch.Status.REJECTED);
            String reason = "Insufficient balance in organization account. Required: ₹" + required 
                + ", Available: ₹" + available;
            batch.setRejectionReason(reason);
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(
                        org.getAdminUser().getEmail(),
                        org.getName(),
                        batch.getPaymentDate().toString(),
                        reason,
                        batch.getId());
            }
            return;
        }

        // 9. Deduct total amount from org account
        locked.setBalance(available.subtract(required));
        bankAccountRepository.save(locked);

        // 10. Mark batch as approved
        batch.setStatus(PaymentBatch.Status.APPROVED);
        batch.setRejectionReason(null);
        batch.setApprovedBy(dto.getReviewer());
        batch.setApprovedAt(Instant.now());
        batchRepository.save(batch);

        // 11. Notify org admin
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchApproved(
                    org.getAdminUser().getEmail(),
                    org.getName(),
                    batch.getPaymentDate().toString(),
                    required.toPlainString(),
                    batch.getId());
        }
    }

    @Override
    @Transactional
    public ExecutionSummary executeApprovedVendorBatch(Long batchId) {
        // 1. Validate batch exists
        PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment batch not found"));

        // 2. Validate batch is vendor type
        if (batch.getType() != PaymentBatch.Type.VENDOR) {
            throw new IllegalStateException("Batch is not a vendor payment batch");
        }

        // 3. Validate batch is in APPROVED status
        if (batch.getStatus() != PaymentBatch.Status.APPROVED) {
            throw new IllegalStateException("Only APPROVED batches can be executed. Current status: " 
                + batch.getStatus());
        }

        List<PaymentLine> lines = lineRepository.findByBatchId(batchId);

        int total = 0, paid = 0, skipped = 0;

        for (PaymentLine line : lines) {
            total++;
            
            // Skip already paid lines (idempotency)
            if (line.getStatus() == PaymentLine.Status.PAID) {
                skipped++;
                continue;
            }

            Vendor vendor = line.getVendor();

            // Generate unique transaction reference
            String txRef = java.util.UUID.randomUUID().toString();

            // Mark line as paid
            line.setStatus(PaymentLine.Status.PAID);
            line.setTransactionRef(txRef);
            line.setProcessedAt(Instant.now());
            lineRepository.save(line);

            paid++;

            // Demo payment execution (replace with actual bank API integration)
            System.out.println("✓ Vendor Payment: ₹" + line.getAmount() 
                + " from " + batch.getOrganization().getName() 
                + " to vendor " + vendor.getName()
                + " [Account: " + vendor.getAccountNumber() + ", IFSC: " + vendor.getIfscCode() + "]"
                + " | TxRef: " + txRef);

            // Optional: Send email notification to vendor if contactEmail exists
//            if (vendor.getContactEmail() != null && !vendor.getContactEmail().isBlank()) {
//                try {
//                    emailService.sendVendorPaymentNotification(
//                            vendor.getContactEmail(),
//                            vendor.getName(),
//                            line.getAmount().toPlainString(),
//                            batch.getPaymentDate().toString(),
//                            batch.getOrganization().getName(),
//                            txRef);
//                } catch (Exception ex) {
//                    System.err.println("Email send failed for vendorId=" + vendor.getId() + ": " + ex.getMessage());
//                }
//            }
        }

        // Mark batch as completed if all lines are paid
        boolean allPaid = lineRepository.findByBatchId(batchId).stream()
                .allMatch(l -> l.getStatus() == PaymentLine.Status.PAID);
        if (allPaid) {
            batch.setStatus(PaymentBatch.Status.COMPLETED);
            batchRepository.save(batch);
        }

        return new ExecutionSummary(total, paid, skipped, batch.getStatus().name());
    }

    private PaymentBatchResponseDTO toResponse(PaymentBatch b) {
        List<PaymentBatchResponseDTO.PaymentLineDTO> lineDtos = b.getLines() == null ? List.of() :
                b.getLines().stream().map(l ->
                        PaymentBatchResponseDTO.PaymentLineDTO.builder()
                                .lineId(l.getId())
                                .vendorId(l.getVendor().getId())
                                .vendorName(l.getVendor().getName())
                                .amount(l.getAmount())
                                .status(l.getStatus().name())
                                .build()
                ).collect(Collectors.toList());

        return PaymentBatchResponseDTO.builder()
                .batchId(b.getId())
                .organizationId(b.getOrganization().getId())
                .type(b.getType().name())
                .paymentDate(b.getPaymentDate())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus().name())
                .lines(lineDtos)
                .build();
    }
}
