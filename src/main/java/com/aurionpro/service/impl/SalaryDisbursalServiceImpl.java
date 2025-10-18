package com.aurionpro.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.SalaryDisbursalApprovalDTO;
import com.aurionpro.dtos.SalaryDisbursalRequestDTO;
import com.aurionpro.dtos.SalaryDisbursalResponseDTO;
import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.SalaryDisbursalRequest;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.BankAccountRepository;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.SalaryDisbursalRequestRepository;
import com.aurionpro.service.NotificationService;
import com.aurionpro.service.SalaryDisbursalService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SalaryDisbursalServiceImpl implements SalaryDisbursalService {

    private final SalaryDisbursalRequestRepository disbursalRequestRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SalaryDisbursalResponseDTO createSalaryDisbursalRequest(Long orgId, SalaryDisbursalRequestDTO requestDTO) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        if (organization.getStatus() != Organization.Status.APPROVED) {
            throw new IllegalStateException("Organization must be approved to create salary requests");
        }

        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with ID: " + requestDTO.getEmployeeId()));

        if (!employee.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Employee does not belong to this organization");
        }

        if (employee.getStatus() != Employee.Status.ACTIVE) {
            throw new IllegalArgumentException("Cannot create salary request for inactive employee");
        }

        LocalDate now = LocalDate.now();
        LocalDate maxAllowedMonth = now.plusMonths(1).withDayOfMonth(1);
        if (requestDTO.getSalaryMonth().isAfter(maxAllowedMonth)) {
            throw new IllegalArgumentException("Cannot create salary request for months beyond " + maxAllowedMonth);
        }

        if (disbursalRequestRepository.existsByEmployeeIdAndSalaryMonth(employee.getId(), requestDTO.getSalaryMonth())) {
            throw new IllegalArgumentException("Salary request already exists for this employee and month: "
                    + requestDTO.getSalaryMonth());
        }

        BankAccount verifiedEmpAccount = bankAccountRepository.findByEmployeeId(employee.getId()).stream()
                .filter(acc -> acc.isVerified()
                        && acc.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.VERIFIED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee does not have a verified bank account. KYC approval required."));

        bankAccountRepository.findByOrganizationId(orgId).stream()
                .filter(acc -> acc.isVerified()
                        && acc.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.VERIFIED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organization does not have a verified payroll bank account"));

        Double amount = calculatePayableSalary(employee);

        SalaryDisbursalRequest request = SalaryDisbursalRequest.builder()
                .organization(organization)
                .employee(employee)
                .amount(amount)
                .salaryMonth(requestDTO.getSalaryMonth())
                .status(SalaryDisbursalRequest.Status.PENDING)
                .build();

        SalaryDisbursalRequest saved = disbursalRequestRepository.save(request);
        return modelMapper.map(saved, SalaryDisbursalResponseDTO.class);
    }

    private Double calculatePayableSalary(Employee employee) {
        if (employee.getSalaryTemplate() == null) {
            throw new IllegalArgumentException("Employee does not have an assigned salary template");
        }

        double basicSalary = employee.getOverrideBasicSalary() != null
                ? employee.getOverrideBasicSalary()
                : employee.getSalaryTemplate().getBasicSalary();

        double hra = employee.getOverrideHra() != null ? employee.getOverrideHra() : employee.getSalaryTemplate().getHra();
        double allowances = employee.getOverrideAllowances() != null ? employee.getOverrideAllowances()
                : employee.getSalaryTemplate().getAllowances();
        double deductions = employee.getOverrideDeductions() != null ? employee.getOverrideDeductions()
                : employee.getSalaryTemplate().getDeductions();

        double total = basicSalary + hra + allowances - deductions;

        if (total <= 0) {
            throw new IllegalArgumentException(
                    "Computed salary amount must be positive. Check salary template or overrides.");
        }

        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalaryDisbursalResponseDTO> getPendingRequestsForBankAdmin(Pageable pageable) {
        return disbursalRequestRepository.findByStatus(SalaryDisbursalRequest.Status.PENDING, pageable)
                .map(req -> modelMapper.map(req, SalaryDisbursalResponseDTO.class));
    }

    @Override
    @Transactional
    public void approveOrRejectRequest(SalaryDisbursalApprovalDTO approvalDTO) {
        SalaryDisbursalRequest request = disbursalRequestRepository
                .findById(approvalDTO.getDisbursalRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Salary disbursal request not found with ID: " + approvalDTO.getDisbursalRequestId()));

        if (request.getStatus() != SalaryDisbursalRequest.Status.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be approved or rejected. Current status: " + request.getStatus());
        }

        if (approvalDTO.getApprove() == null) {
            throw new IllegalArgumentException("Approval decision must be provided");
        }

        if (!approvalDTO.getApprove()) {
            if (approvalDTO.getRejectionReason() == null || approvalDTO.getRejectionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting salary request");
            }

            request.setStatus(SalaryDisbursalRequest.Status.REJECTED);
            request.setRejectionReason(approvalDTO.getRejectionReason().trim());
            disbursalRequestRepository.save(request);

            notificationService.notifyDisbursalRejection(request.getEmployee().getId(),
                    approvalDTO.getRejectionReason());
            return;
        }

        executeApproval(request);
    }

    private void executeApproval(SalaryDisbursalRequest request) {
        Long orgId = request.getOrganization().getId();

        BankAccount orgAccount = bankAccountRepository.findFirstVerifiedOrgAccount(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization has no verified payroll account"));

        BankAccount locked = bankAccountRepository.findByIdForUpdate(orgAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll account not found"));

        BigDecimal available = locked.getBalance();
        BigDecimal required = BigDecimal.valueOf(request.getAmount());

        if (available.compareTo(required) < 0) {
            request.setStatus(SalaryDisbursalRequest.Status.REJECTED);
            request.setRejectionReason(
                    "Insufficient balance in organization account. Required: ₹" + required + ", Available: ₹" + available);
            disbursalRequestRepository.save(request);

            notificationService.notifyDisbursalRejection(request.getEmployee().getId(), request.getRejectionReason());
            return;
        }

        locked.setBalance(available.subtract(required));
        bankAccountRepository.save(locked);

        request.setStatus(SalaryDisbursalRequest.Status.APPROVED);
        request.setRejectionReason(null);
        disbursalRequestRepository.save(request);

        demoExecutePayment(request);

        notificationService.notifyDisbursalApproval(request.getEmployee().getId(), request.getAmount());
    }

    private void demoExecutePayment(SalaryDisbursalRequest request) {
        System.out.println("✓ Salary Transfer: ₹" + request.getAmount() + " from "
                + request.getOrganization().getName() + " to employee " + request.getEmployee().getFullName()
                + " for month " + request.getSalaryMonth());
    }
}
