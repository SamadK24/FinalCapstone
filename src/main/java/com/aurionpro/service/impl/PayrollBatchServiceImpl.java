package com.aurionpro.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.DisbursalBatchApprovalDTO;
import com.aurionpro.dtos.DisbursalBatchCreateDTO;
import com.aurionpro.dtos.DisbursalBatchResponseDTO;
import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.DisbursalBatch;
import com.aurionpro.entity.DisbursalLine;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.Payslip;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.BankAccountRepository;
import com.aurionpro.repository.DisbursalBatchRepository;
import com.aurionpro.repository.DisbursalLineRepository;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.PayslipRepository;
import com.aurionpro.service.EmailService;
import com.aurionpro.service.PayrollBatchService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollBatchServiceImpl implements PayrollBatchService {

    private final DisbursalBatchRepository batchRepository;
    private final DisbursalLineRepository lineRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final PayslipRepository payslipRepository;

    @Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;

    // ------------------ CREATE BATCH ------------------
    @Override
    @Transactional
    public DisbursalBatchResponseDTO createBatch(Long orgId, DisbursalBatchCreateDTO dto, String createdBy) {
        // 1. Validate organization exists and is approved
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));
        
        if (org.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization must be approved to create payroll batches");
        }
        
        // 2. Validate salary month is provided
        if (dto.getSalaryMonth() == null) {
            throw new IllegalArgumentException("Salary month is required");
        }
        
        // 3. Check for duplicate batch for same org and month
        if (batchRepository.existsByOrganizationIdAndSalaryMonth(orgId, dto.getSalaryMonth())) {
            throw new IllegalArgumentException("Salary batch already exists for this organization and month: " 
                + dto.getSalaryMonth());
        }
        
        // 4. Validate organization has verified payroll account
        bankAccountRepository.findFirstVerifiedOrgAccount(orgId)
                .orElseThrow(() -> new IllegalStateException(
                    "Organization does not have a verified payroll bank account"));
        
        // 5. Get employee list (specific IDs or all org employees)
        List<Employee> employees = (dto.getEmployeeIds() != null && !dto.getEmployeeIds().isEmpty())
                ? employeeRepository.findAllById(dto.getEmployeeIds()).stream()
                        .filter(e -> e.getOrganization().getId().equals(orgId))
                        .collect(Collectors.toList())
                : org.getEmployees() == null
                        ? List.of()
                        : org.getEmployees().stream()
                                .collect(Collectors.toList());

        // 6. Filter only eligible employees (ACTIVE + has salary template + verified bank account)
        List<Employee> eligibleEmployees = employees.stream()
                .filter(e -> e.getStatus() == Employee.Status.ACTIVE)
                .filter(e -> e.getSalaryTemplate() != null)
                .filter(e -> hasVerifiedBankAccount(e.getId()))
                .collect(Collectors.toList());

        if (eligibleEmployees.isEmpty()) {
            throw new IllegalArgumentException("No eligible employees found. Employees must be ACTIVE, have salary template, and verified bank account.");
        }

        // 7. Create disbursal lines
        HashSet<DisbursalLine> lines = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        
        for (Employee e : eligibleEmployees) {
            BigDecimal amt = computeNet(e);
            DisbursalLine line = DisbursalLine.builder()
                    .employee(e)
                    .amount(amt)
                    .status(DisbursalLine.Status.QUEUED)
                    .build();
            lines.add(line);
            total = total.add(amt);
        }

        // 8. Create batch
        DisbursalBatch batch = DisbursalBatch.builder()
                .organization(org)
                .salaryMonth(dto.getSalaryMonth())
                .totalAmount(total)
                .status(DisbursalBatch.Status.PENDING)
                .createdBy(createdBy)
                .build();

        lines.forEach(l -> l.setBatch(batch));
        batch.setLines(lines);

        DisbursalBatch saved = batchRepository.save(batch);

        // 9. Send notifications
        if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
            emailService.sendBatchPendingReview(
                    bankAdminInbox,
                    org.getName(),
                    dto.getSalaryMonth().toString(),
                    total.toPlainString(),
                    saved.getId());
        }

        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchAcknowledgedToOrg(
                    org.getAdminUser().getEmail(),
                    org.getName(),
                    dto.getSalaryMonth().toString(),
                    total.toPlainString(),
                    saved.getId());
        }

        return toResponse(saved);
    }

    private boolean hasVerifiedBankAccount(Long employeeId) {
        return bankAccountRepository.findByEmployeeId(employeeId).stream()
            .anyMatch(acc -> acc.isVerified() 
                && acc.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.VERIFIED);
    }


    // ------------------ LIST PENDING ------------------
    @Override
    @Transactional(readOnly = true)
    public Page<DisbursalBatchResponseDTO> listPendingBatchesForBankAdmin(Pageable pageable) {
        Page<DisbursalBatch> batches = batchRepository.findByStatus(DisbursalBatch.Status.PENDING, pageable);
        return batches.map(this::toResponse);
    }




    // ------------------ REVIEW BATCH ------------------
    @Override
    @Transactional
    public void reviewBatch(DisbursalBatchApprovalDTO dto) {
        // 1. Validate batch exists
        DisbursalBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with ID: " + dto.getBatchId()));

        // 2. Validate batch is in PENDING status
        if (batch.getStatus() != DisbursalBatch.Status.PENDING) {
            throw new IllegalStateException("Only PENDING batches can be reviewed. Current status: " 
                + batch.getStatus());
        }

        // 3. Validate approval decision is provided
        if (dto.getApprove() == null) {
            throw new IllegalArgumentException("Approval decision must be provided");
        }

        Organization org = batch.getOrganization();

        // 4. Handle rejection
        if (!Boolean.TRUE.equals(dto.getApprove())) {
            // Rejection reason is mandatory
            if (dto.getRejectionReason() == null || dto.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting batch");
            }
            
            batch.setStatus(DisbursalBatch.Status.REJECTED);
            batch.setRejectionReason(dto.getRejectionReason().trim());
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(
                        org.getAdminUser().getEmail(),
                        org.getName(),
                        batch.getSalaryMonth().toString(),
                        dto.getRejectionReason(),
                        batch.getId());
            }
            return;
        }

        // 5. Handle approval - check org has verified payroll account
        BankAccount orgAccount = bankAccountRepository.findFirstVerifiedOrgAccount(org.getId())
                .orElseThrow(() -> new IllegalStateException("Organization has no verified payroll account"));

        // 6. Lock account for update
        BankAccount locked = bankAccountRepository.findByIdForUpdate(orgAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll account not found"));

        // 7. Check sufficient balance
        BigDecimal available = locked.getBalance();
        BigDecimal required = batch.getTotalAmount();

        if (available.compareTo(required) < 0) {
            batch.setStatus(DisbursalBatch.Status.REJECTED);
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
                        batch.getSalaryMonth().toString(),
                        reason,
                        batch.getId());
            }
            return;
        }

        // 8. Deduct total amount from org account
        locked.setBalance(available.subtract(required));
        bankAccountRepository.save(locked);

        // 9. Mark batch as approved
        batch.setStatus(DisbursalBatch.Status.APPROVED);
        batch.setRejectionReason(null);
        batch.setApprovedBy(dto.getReviewer());
        batch.setApprovedAt(Instant.now());
        batchRepository.save(batch);

        // 10. Notify org admin
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchApproved(
                    org.getAdminUser().getEmail(),
                    org.getName(),
                    batch.getSalaryMonth().toString(),
                    required.toPlainString(),
                    batch.getId());
        }
    }


    // ------------------ EXECUTE APPROVED BATCH ------------------
    @Override
    @Transactional
    public ExecutionSummary executeApprovedBatch(Long batchId) {
        DisbursalBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        if (batch.getStatus() != DisbursalBatch.Status.APPROVED) {
            throw new IllegalStateException("Only APPROVED batches can be executed");
        }

        List<DisbursalLine> lines = lineRepository.findByBatchId(batchId);

        int total = 0, paid = 0, skipped = 0;

        for (DisbursalLine line : lines) {
            total++;
            boolean alreadyPaid = line.getStatus() == DisbursalLine.Status.PAID;
            boolean payslipExists = payslipRepository.findByLineId(line.getId()).isPresent();
            if (alreadyPaid && payslipExists) {
                skipped++;
                continue;
            }

            Employee e = line.getEmployee();
            ComponentBreakdown comp = computeComponents(e);

            String txRef = java.util.UUID.randomUUID().toString();

            if (!alreadyPaid) {
                line.setStatus(DisbursalLine.Status.PAID);
                line.setTransactionRef(txRef);
                line.setProcessedAt(Instant.now());
                lineRepository.save(line);
            } else if (line.getTransactionRef() == null || line.getTransactionRef().isBlank()) {
                line.setTransactionRef(txRef);
                lineRepository.save(line);
            } else {
                txRef = line.getTransactionRef();
            }

            if (!payslipExists) {
                Payslip slip = Payslip.builder()
                        .employee(e)
                        .organization(batch.getOrganization())
                        .batch(batch)
                        .line(line)
                        .salaryMonth(batch.getSalaryMonth())
                        .basic(comp.getBasic())
                        .hra(comp.getHra())
                        .allowances(comp.getAllowances())
                        .deductions(comp.getDeductions())
                        .netAmount(comp.getNet())
                        .transactionRef(txRef)
                        .build();
                payslipRepository.save(slip);
            }

            paid++;

            if (e.getEmail() != null) {
                try {
                    Payslip created = payslipRepository.findByLineId(line.getId()).orElse(null);
                    if (created != null) {
                        emailService.sendSalaryCreditedWithPayslipLink(
                                e.getEmail(),
                                e.getFullName(),
                                comp.getNet().toPlainString(),
                                batch.getSalaryMonth().toString(),
                                batch.getOrganization().getName(),
                                created.getId(),
                                line.getTransactionRef());
                    }
                } catch (Exception ex) {
                    System.err.println("Email send failed for employeeId=" + e.getId() + ": " + ex.getMessage());
                }
            }
        }

        boolean allPaid = lineRepository.findByBatchId(batchId).stream()
                .allMatch(l -> l.getStatus() == DisbursalLine.Status.PAID);
        if (allPaid) {
            batch.setStatus(DisbursalBatch.Status.COMPLETED);
            batchRepository.save(batch);
        }

        return new ExecutionSummary(total, paid, skipped, batch.getStatus().name());
    }

    // ------------------ HELPERS ------------------
    private BigDecimal computeNet(Employee e) {
        if (e.getSalaryTemplate() == null) {
            throw new IllegalArgumentException("Employee " + e.getId() + " has no salary template");
        }
        double basic = e.getOverrideBasicSalary() != null ? e.getOverrideBasicSalary() : e.getSalaryTemplate().getBasicSalary();
        double hra = e.getOverrideHra() != null ? e.getOverrideHra() : e.getSalaryTemplate().getHra();
        double allow = e.getOverrideAllowances() != null ? e.getOverrideAllowances() : e.getSalaryTemplate().getAllowances();
        double ded = e.getOverrideDeductions() != null ? e.getOverrideDeductions() : e.getSalaryTemplate().getDeductions();
        double total = basic + hra + allow - ded;
        if (total < 0) throw new IllegalArgumentException("Computed salary cannot be negative for employee " + e.getId());
        return BigDecimal.valueOf(total).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private ComponentBreakdown computeComponents(Employee e) {
        double basic = e.getOverrideBasicSalary() != null ? e.getOverrideBasicSalary() : e.getSalaryTemplate().getBasicSalary();
        double hra = e.getOverrideHra() != null ? e.getOverrideHra() : e.getSalaryTemplate().getHra();
        double allow = e.getOverrideAllowances() != null ? e.getOverrideAllowances() : e.getSalaryTemplate().getAllowances();
        double ded = e.getOverrideDeductions() != null ? e.getOverrideDeductions() : e.getSalaryTemplate().getDeductions();

        if (basic + hra + allow - ded < 0) throw new IllegalArgumentException("Computed salary cannot be negative for employee " + e.getId());

        return new ComponentBreakdown(
                bd(basic),
                bd(hra),
                bd(allow),
                bd(ded),
                bd(basic + hra + allow - ded)
        );
    }

    private BigDecimal bd(double d) {
        return BigDecimal.valueOf(d).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private DisbursalBatchResponseDTO toResponse(DisbursalBatch b) {
        List<DisbursalBatchResponseDTO.DisbursalLineDTO> lineDtos = b.getLines() == null ? List.of() :
                b.getLines().stream().map(l ->
                        DisbursalBatchResponseDTO.DisbursalLineDTO.builder()
                                .lineId(l.getId())
                                .employeeId(l.getEmployee().getId())
                                .employeeName(l.getEmployee().getFullName())
                                .amount(l.getAmount())
                                .status(l.getStatus().name())
                                .build()
                ).collect(Collectors.toList());

        return DisbursalBatchResponseDTO.builder()
                .batchId(b.getId())
                .organizationId(b.getOrganization().getId())
                .salaryMonth(b.getSalaryMonth())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus().name())
                .lines(lineDtos)
                .build();
    }

    @Getter
    @AllArgsConstructor
    private static class ComponentBreakdown {
        private final BigDecimal basic;
        private final BigDecimal hra;
        private final BigDecimal allowances;
        private final BigDecimal deductions;
        private final BigDecimal net;
    }
}

