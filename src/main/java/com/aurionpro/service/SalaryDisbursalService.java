package com.aurionpro.service;

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

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryDisbursalService {

    private final SalaryDisbursalRequestRepository disbursalRequestRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Transactional
    public SalaryDisbursalResponseDTO createSalaryDisbursalRequest(Long orgId, SalaryDisbursalRequestDTO requestDTO) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Employee does not belong to this organization");
        }

        List<BankAccount> employeeAccounts = bankAccountRepository.findByEmployeeId(employee.getId());
        BankAccount verifiedEmpAccount = employeeAccounts.stream()
                .filter(acc -> acc.isVerified() && acc.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.VERIFIED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Employee does not have a verified bank account"));

        List<BankAccount> orgPayrollAccounts = bankAccountRepository.findByOrganizationId(orgId);
        BankAccount verifiedOrgAccount = orgPayrollAccounts.stream()
                .filter(acc -> acc.isVerified() && acc.getKycStatus() == BankAccount.KYCDocumentVerificationStatus.VERIFIED)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Organization does not have a verified payroll bank account"));

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
            throw new IllegalArgumentException("Employee does not have an assigned salary template.");
        }

        double basicSalary = employee.getOverrideBasicSalary() != null ? employee.getOverrideBasicSalary() : employee.getSalaryTemplate().getBasicSalary();
        double hra = employee.getOverrideHra() != null ? employee.getOverrideHra() : employee.getSalaryTemplate().getHra();
        double allowances = employee.getOverrideAllowances() != null ? employee.getOverrideAllowances() : employee.getSalaryTemplate().getAllowances();
        double deductions = employee.getOverrideDeductions() != null ? employee.getOverrideDeductions() : employee.getSalaryTemplate().getDeductions();

        double total = basicSalary + hra + allowances - deductions;

        if (total < 0) {
            throw new IllegalArgumentException("Computed salary amount cannot be negative.");
        }

        return total;
    }

    public List<SalaryDisbursalResponseDTO> getPendingRequestsForBankAdmin() {
        List<SalaryDisbursalRequest> requests = disbursalRequestRepository.findByStatus(SalaryDisbursalRequest.Status.PENDING);
        return requests.stream()
                .map(r -> modelMapper.map(r, SalaryDisbursalResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveOrRejectRequest(SalaryDisbursalApprovalDTO approvalDTO) {
        SalaryDisbursalRequest request = disbursalRequestRepository.findById(approvalDTO.getDisbursalRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Salary disbursal request not found"));

        if (approvalDTO.getApprove() == null) {
            throw new IllegalArgumentException("Approval decision must be provided");
        }

        if (!approvalDTO.getApprove()) {
            request.setStatus(SalaryDisbursalRequest.Status.REJECTED);
            request.setRejectionReason(approvalDTO.getRejectionReason());
            disbursalRequestRepository.save(request);
            notificationService.notifyDisbursalRejection(request.getEmployee().getId(), approvalDTO.getRejectionReason());
            return;
        }

        if (request.getStatus() != SalaryDisbursalRequest.Status.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }

        Long orgId = request.getOrganization().getId();

        // Choose a verified payroll account for the organization
        BankAccount orgAccount = bankAccountRepository.findFirstVerifiedOrgAccount(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization has no verified payroll account"));

        // Lock the account row to prevent concurrent overspend
        BankAccount locked = bankAccountRepository.findByIdForUpdate(orgAccount.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Payroll account not found"));

        // Use BigDecimal for currency math
        java.math.BigDecimal available = new java.math.BigDecimal(locked.getBalance().toString());
        java.math.BigDecimal required = java.math.BigDecimal.valueOf(request.getAmount());

        if (available.compareTo(required) < 0) {
            request.setStatus(SalaryDisbursalRequest.Status.REJECTED);
            request.setRejectionReason("Insufficient balance: required " + required + ", available " + available);
            disbursalRequestRepository.save(request);
            notificationService.notifyDisbursalRejection(request.getEmployee().getId(), request.getRejectionReason());
            return;
        }

        // Debit and persist
        java.math.BigDecimal newBalance = available.subtract(required);
        locked.setBalance(newBalance);
        bankAccountRepository.save(locked);

        // Mark request approved and run the existing payment demo
        request.setStatus(SalaryDisbursalRequest.Status.APPROVED);
        request.setRejectionReason(null);
        disbursalRequestRepository.save(request);

        demoExecutePayment(request);

        notificationService.notifyDisbursalApproval(request.getEmployee().getId(), request.getAmount());
    }


    private void demoExecutePayment(SalaryDisbursalRequest request) {
        System.out.println("Transferring " + request.getAmount() + " from org " + request.getOrganization().getName() +
                " payroll account to employee " + request.getEmployee().getFullName());
    }
}
