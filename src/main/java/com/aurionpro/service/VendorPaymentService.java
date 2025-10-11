package com.aurionpro.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aurionpro.dtos.PaymentBatchApprovalDTO;
import com.aurionpro.dtos.PaymentBatchCreateDTO;
import com.aurionpro.dtos.PaymentBatchResponseDTO;
import com.aurionpro.entity.*;
import com.aurionpro.entity.PaymentBatch.Status;
import com.aurionpro.entity.PaymentBatch.Type;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorPaymentService {

    private final PaymentBatchRepository batchRepository;
    private final PaymentLineRepository lineRepository;
    private final OrganizationRepository organizationRepository;
    private final VendorRepository vendorRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;

    @Transactional
    public PaymentBatchResponseDTO createVendorPaymentBatch(Long orgId, PaymentBatchCreateDTO dto, String createdBy) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (org.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization is not approved");
        }
        if (dto.getPaymentDate() == null || dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new IllegalArgumentException("paymentDate and lines are required");
        }

        // Validate vendors and build lines
        Set<PaymentLine> lines = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentBatchCreateDTO.Line in : dto.getLines()) {
            Vendor v = vendorRepository.findByIdAndOrganizationId(in.getVendorId(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vendor not found or not in org"));
            if (v.getStatus() != Vendor.Status.ACTIVE || v.getKycStatus() != BankAccount.KYCDocumentVerificationStatus.VERIFIED) {
                throw new IllegalArgumentException("Vendor not ACTIVE/VERIFIED: " + v.getId());
            }
            if (in.getAmount() == null || in.getAmount().signum() <= 0) {
                throw new IllegalArgumentException("Amount must be positive for vendor: " + v.getId());
            }
            PaymentLine l = PaymentLine.builder()
                    .vendor(v)
                    .amount(in.getAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                    .status(PaymentLine.Status.QUEUED)
                    .build();
            lines.add(l);
            total = total.add(l.getAmount());
        }

        PaymentBatch batch = PaymentBatch.builder()
                .organization(org)
                .type(Type.VENDOR)
                .paymentDate(dto.getPaymentDate())
                .totalAmount(total)
                .status(Status.PENDING)
                .createdBy(createdBy)
                .build();

        lines.forEach(l -> l.setBatch(batch));
        batch.setLines(lines);

        PaymentBatch saved = batchRepository.save(batch);

        // Notify reviewer + org admin
        if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
            emailService.sendBatchPendingReview(bankAdminInbox, org.getName(),
                    dto.getPaymentDate().toString(), total.toPlainString(), saved.getId());
        }
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchAcknowledgedToOrg(org.getAdminUser().getEmail(), org.getName(),
                    dto.getPaymentDate().toString(), total.toPlainString(), saved.getId());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentBatchResponseDTO> listPendingForBankAdmin() {
        return batchRepository.findByStatus(Status.PENDING).stream()
                .filter(b -> b.getType() == Type.VENDOR)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reviewVendorBatch(PaymentBatchApprovalDTO dto) {
        PaymentBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment batch not found"));

        if (batch.getType() != Type.VENDOR) {
            throw new IllegalArgumentException("Batch is not VENDOR type");
        }
        if (batch.getStatus() != Status.PENDING) {
            throw new IllegalStateException("Only PENDING batches can be reviewed");
        }

        if (!Boolean.TRUE.equals(dto.getApprove())) {
            batch.setStatus(Status.REJECTED);
            batch.setRejectionReason(dto.getRejectionReason());
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            Organization org = batch.getOrganization();
            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(org.getAdminUser().getEmail(), org.getName(),
                        batch.getPaymentDate().toString(), dto.getRejectionReason(), batch.getId());
            }
            return;
        }

        // Approval path: lock and debit once from verified org account
        Long orgId = batch.getOrganization().getId();
        BankAccount orgAccount = bankAccountRepository.findFirstVerifiedOrgAccount(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization has no verified payroll account"));

        BankAccount locked = bankAccountRepository.findByIdForUpdate(orgAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll account not found"));

        BigDecimal available = locked.getBalance();
        BigDecimal required = batch.getTotalAmount();

        if (available.compareTo(required) < 0) {
            batch.setStatus(Status.REJECTED);
            String reason = "Insufficient balance: required " + required + ", available " + available;
            batch.setRejectionReason(reason);
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            Organization org = batch.getOrganization();
            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(org.getAdminUser().getEmail(), org.getName(),
                        batch.getPaymentDate().toString(), reason, batch.getId());
            }
            return;
        }

        // Debit and mark approved
        locked.setBalance(available.subtract(required));
        bankAccountRepository.save(locked);

        batch.setStatus(Status.APPROVED);
        batch.setRejectionReason(null);
        batch.setApprovedBy(dto.getReviewer());
        batch.setApprovedAt(Instant.now());
        batchRepository.save(batch);

        Organization org = batch.getOrganization();
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchApproved(org.getAdminUser().getEmail(), org.getName(),
                    batch.getPaymentDate().toString(), required.toPlainString(), batch.getId());
        }
    }

    @Transactional
    public ExecutionSummary executeApprovedVendorBatch(Long batchId) {
        PaymentBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment batch not found"));

        if (batch.getType() != Type.VENDOR) {
            throw new IllegalArgumentException("Batch is not VENDOR type");
        }
        if (batch.getStatus() != Status.APPROVED) {
            throw new IllegalStateException("Only APPROVED batches can be executed");
        }

        List<PaymentLine> lines = lineRepository.findByBatchId(batchId);
        int total = 0, paid = 0, skipped = 0;

        for (PaymentLine line : lines) {
            total++;
            boolean alreadyPaid = line.getStatus() == PaymentLine.Status.PAID;
            if (alreadyPaid && line.getTransactionRef() != null && !line.getTransactionRef().isBlank()) {
                skipped++;
                continue;
            }

            String txRef = java.util.UUID.randomUUID().toString();
            if (!alreadyPaid) {
                line.setStatus(PaymentLine.Status.PAID);
                line.setTransactionRef(txRef);
                line.setProcessedAt(Instant.now());
                lineRepository.save(line);
            } else {
                if (line.getTransactionRef() == null || line.getTransactionRef().isBlank()) {
                    line.setTransactionRef(txRef);
                    lineRepository.save(line);
                }
            }
            paid++;

            // Optional: notify vendor contact
            Vendor v = line.getVendor();
            if (v != null && v.getContactEmail() != null) {
                try {
                    emailService.sendSalaryCreditedWithPayslipLink( // reuse method signature; or add a dedicated one later
                            v.getContactEmail(),
                            v.getName(),
                            line.getAmount().toPlainString(),
                            batch.getPaymentDate().toString(),
                            batch.getOrganization().getName(),
                            null, // no payslipId for vendors
                            line.getTransactionRef()
                    );
                } catch (Exception ignore) {}
            }
        }

        boolean allPaid = lineRepository.findByBatchId(batchId).stream()
                .allMatch(l -> l.getStatus() == PaymentLine.Status.PAID);
        if (allPaid) {
            batch.setStatus(Status.COMPLETED);
            batchRepository.save(batch);
        }

        return new ExecutionSummary(total, paid, skipped, batch.getStatus().name());
    }

    private PaymentBatchResponseDTO toResponse(PaymentBatch b) {
        var lineDtos = b.getLines() == null ? List.<PaymentBatchResponseDTO.PaymentLineDTO>of()
                : b.getLines().stream().map(l ->
                    PaymentBatchResponseDTO.PaymentLineDTO.builder()
                        .lineId(l.getId())
                        .vendorId(l.getVendor() != null ? l.getVendor().getId() : null)
                        .vendorName(l.getVendor() != null ? l.getVendor().getName() : null)
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

    @Getter @AllArgsConstructor
    public static class ExecutionSummary {
        private final int totalLines;
        private final int paidLines;
        private final int skippedLines;
        private final String batchStatus;
    }
}

