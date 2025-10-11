package com.aurionpro.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
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
import com.aurionpro.repository.SalaryStructureRepository;
import com.aurionpro.repository.SalaryTemplateRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollBatchService {

    private final DisbursalBatchRepository batchRepository;
    private final DisbursalLineRepository lineRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final BankAccountRepository bankAccountRepository;
    private final SalaryTemplateRepository salaryTemplateRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final PayslipRepository payslipRepository;

    @Value("${notifications.bank.admin.to:}")
    private String bankAdminInbox;

    // Reuse existing salary logic consistently with single-request path
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

    @Transactional
    public DisbursalBatchResponseDTO createBatch(Long orgId, DisbursalBatchCreateDTO dto, String createdBy) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        if (org.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization is not approved");
        }
        if (dto.getSalaryMonth() == null) {
            throw new IllegalArgumentException("salaryMonth is required");
        }

        // Get employees
        List<Employee> employees;
        if (dto.getEmployeeIds() != null && !dto.getEmployeeIds().isEmpty()) {
            employees = employeeRepository.findAllById(dto.getEmployeeIds()).stream()
                    .filter(e -> e.getOrganization().getId().equals(orgId))
                    .collect(Collectors.toList());
        } else {
            // All org employees that have a salary template
            employees = org.getEmployees() == null ? List.of() : org.getEmployees().stream()
                    .filter(e -> e.getSalaryTemplate() != null)
                    .collect(Collectors.toList());
        }
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No eligible employees for batch");
        }

        // Build lines and total
        Set<DisbursalLine> lines = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Employee e : employees) {
            BigDecimal amt = computeNet(e);
            DisbursalLine line = DisbursalLine.builder()
                    .employee(e)
                    .amount(amt)
                    .status(DisbursalLine.Status.QUEUED)
                    .build();
            lines.add(line);
            total = total.add(amt);
        }

        DisbursalBatch batch = DisbursalBatch.builder()
                .organization(org)
                .salaryMonth(dto.getSalaryMonth())
                .totalAmount(total)
                .status(DisbursalBatch.Status.PENDING)
                .createdBy(createdBy)
                .build();

        // Link lines to batch
        lines.forEach(l -> l.setBatch(batch));
        batch.setLines(lines);

        DisbursalBatch saved = batchRepository.save(batch);

        // Notify bank admin inbox and acknowledge org admin
        if (bankAdminInbox != null && !bankAdminInbox.isBlank()) {
            emailService.sendBatchPendingReview(
                    bankAdminInbox,
                    org.getName(),
                    dto.getSalaryMonth().toString(),
                    total.toPlainString(),
                    saved.getId()
            );
        }
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchAcknowledgedToOrg(
                    org.getAdminUser().getEmail(),
                    org.getName(),
                    dto.getSalaryMonth().toString(),
                    total.toPlainString(),
                    saved.getId()
            );
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DisbursalBatchResponseDTO> listPendingBatchesForBankAdmin() {
        return batchRepository.findByStatus(DisbursalBatch.Status.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reviewBatch(DisbursalBatchApprovalDTO dto) {
        DisbursalBatch batch = batchRepository.findById(dto.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        if (batch.getStatus() != DisbursalBatch.Status.PENDING) {
            throw new IllegalStateException("Only PENDING batches can be reviewed");
        }

        if (!Boolean.TRUE.equals(dto.getApprove())) {
            batch.setStatus(DisbursalBatch.Status.REJECTED);
            batch.setRejectionReason(dto.getRejectionReason());
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            // notify org admin
            Organization org = batch.getOrganization();
            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(
                        org.getAdminUser().getEmail(),
                        org.getName(),
                        batch.getSalaryMonth().toString(),
                        dto.getRejectionReason(),
                        batch.getId()
                );
            }
            return;
        }

        // Approval path: lock and debit once
        Long orgId = batch.getOrganization().getId();
        BankAccount orgAccount = bankAccountRepository.findFirstVerifiedOrgAccount(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization has no verified payroll account"));

        BankAccount locked = bankAccountRepository.findByIdForUpdate(orgAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll account not found"));

        BigDecimal available = locked.getBalance();
        BigDecimal required = batch.getTotalAmount();

        if (available.compareTo(required) < 0) {
            batch.setStatus(DisbursalBatch.Status.REJECTED);
            String reason = "Insufficient balance: required " + required + ", available " + available;
            batch.setRejectionReason(reason);
            batch.setApprovedBy(dto.getReviewer());
            batch.setApprovedAt(Instant.now());
            batchRepository.save(batch);

            // notify org admin
            Organization org = batch.getOrganization();
            if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
                emailService.sendBatchRejected(
                        org.getAdminUser().getEmail(),
                        org.getName(),
                        batch.getSalaryMonth().toString(),
                        reason,
                        batch.getId()
                );
            }
            return;
        }

        // Debit and mark approved (COMPLETED optional; staying APPROVED per scope)
        locked.setBalance(available.subtract(required));
        bankAccountRepository.save(locked);

        batch.setStatus(DisbursalBatch.Status.APPROVED);
        batch.setRejectionReason(null);
        batch.setApprovedBy(dto.getReviewer());
        batch.setApprovedAt(Instant.now());
        batchRepository.save(batch);

        // Notify org admin approved
        Organization org = batch.getOrganization();
        if (org.getAdminUser() != null && org.getAdminUser().getEmail() != null) {
            emailService.sendBatchApproved(
                    org.getAdminUser().getEmail(),
                    org.getName(),
                    batch.getSalaryMonth().toString(),
                    required.toPlainString(),
                    batch.getId()
            );
        }
        // Lines remain QUEUED; real credit step will be implemented next phase.
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
    private ComponentBreakdown computeComponents(Employee e) {
        if (e.getSalaryTemplate() == null) {
            throw new IllegalArgumentException("Employee " + e.getId() + " has no salary template");
        }
        double basic = e.getOverrideBasicSalary() != null ? e.getOverrideBasicSalary() : e.getSalaryTemplate().getBasicSalary();
        double hra = e.getOverrideHra() != null ? e.getOverrideHra() : e.getSalaryTemplate().getHra();
        double allow = e.getOverrideAllowances() != null ? e.getOverrideAllowances() : e.getSalaryTemplate().getAllowances();
        double ded = e.getOverrideDeductions() != null ? e.getOverrideDeductions() : e.getSalaryTemplate().getDeductions();

        if (basic + hra + allow - ded < 0) {
            throw new IllegalArgumentException("Computed salary cannot be negative for employee " + e.getId());
        }

        return new ComponentBreakdown(
                bd(basic),
                bd(hra),
                bd(allow),
                bd(ded),
                bd(basic + hra + allow - ded)
        );
    }

    private java.math.BigDecimal bd(double d) {
        return java.math.BigDecimal.valueOf(d).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Getter
    @AllArgsConstructor
    private static class ComponentBreakdown {
        private final java.math.BigDecimal basic;
        private final java.math.BigDecimal hra;
        private final java.math.BigDecimal allowances;
        private final java.math.BigDecimal deductions;
        private final java.math.BigDecimal net;
    }

    @Transactional
    public ExecutionSummary executeApprovedBatch(Long batchId) {
        DisbursalBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new com.aurionpro.exceptions.ResourceNotFoundException("Batch not found"));

        if (batch.getStatus() != DisbursalBatch.Status.APPROVED) {
            throw new IllegalStateException("Only APPROVED batches can be executed");
        }

        // Load lines
        java.util.List<DisbursalLine> lines = lineRepository.findByBatchId(batchId);

        int total = 0;
        int paid = 0;
        int skipped = 0;

        for (DisbursalLine line : lines) {
            total++;

            // Idempotency: if already PAID and payslip exists, skip
            boolean alreadyPaid = line.getStatus() == DisbursalLine.Status.PAID;
            boolean payslipExists = payslipRepository.findByLineId(line.getId()).isPresent();
            if (alreadyPaid && payslipExists) {
                skipped++;
                continue;
            }

            // Compute components snapshot for payslip (or reuse snapshot if you later add component fields to DisbursalLine)
            Employee e = line.getEmployee();
            ComponentBreakdown comp = computeComponents(e);

            // Generate transaction reference
            String txRef = java.util.UUID.randomUUID().toString();

            // Mark line PAID if not already
            if (!alreadyPaid) {
                line.setStatus(DisbursalLine.Status.PAID);
                line.setTransactionRef(txRef);
                line.setProcessedAt(java.time.Instant.now());
                lineRepository.save(line);
            } else {
                // If already PAID but no payslip, reuse the stored transactionRef if present; otherwise set
                if (line.getTransactionRef() == null || line.getTransactionRef().isBlank()) {
                    line.setTransactionRef(txRef);
                    lineRepository.save(line);
                } else {
                    txRef = line.getTransactionRef();
                }
            }

            // Create payslip if not present
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

            // Optional: notify employee about credit with payslip link (implement method in EmailService)
            if (e.getEmail() != null) {
                try {
                    // Find the payslip just created
                    Payslip created = payslipRepository.findByLineId(line.getId()).orElse(null);
                    if (created != null) {
                        emailService.sendSalaryCreditedWithPayslipLink(
                                e.getEmail(),
                                e.getFullName(),
                                comp.getNet().toPlainString(),
                                batch.getSalaryMonth().toString(),
                                batch.getOrganization().getName(),
                                created.getId(),
                                line.getTransactionRef()
                        );
                    }
                } catch (Exception mailEx) {
                    // Log and continue; do not fail execution on email issues
                    System.err.println("Email send failed for employeeId=" + e.getId() + ": " + mailEx.getMessage());
                }
            }
        }

        // Optionally mark batch COMPLETED once all lines are PAID
        boolean allPaid = lineRepository.findByBatchId(batchId).stream()
                .allMatch(l -> l.getStatus() == DisbursalLine.Status.PAID);
        if (allPaid) {
            batch.setStatus(DisbursalBatch.Status.COMPLETED);
            batchRepository.save(batch);
        }

        return new ExecutionSummary(total, paid, skipped, batch.getStatus().name());
    }

    @Getter
    @AllArgsConstructor
    public static class ExecutionSummary {
        private final int totalLines;
        private final int paidLines;
        private final int skippedLines;
        private final String batchStatus;
    }
}

