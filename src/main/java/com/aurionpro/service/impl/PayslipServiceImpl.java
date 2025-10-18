package com.aurionpro.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.EmployeePayslipView;
import com.aurionpro.entity.BankAccount;
import com.aurionpro.entity.Employee;
import com.aurionpro.entity.Payslip;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.BankAccountRepository;
import com.aurionpro.repository.EmployeeRepository;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.PayslipRepository;
import com.aurionpro.service.PayslipService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayslipServiceImpl implements PayslipService {

    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ModelMapper modelMapper;

    private static final DateTimeFormatter PERIOD_LABEL = DateTimeFormatter.ofPattern("MMM yyyy");

    @Override
    @Transactional(readOnly = true)
    public EmployeePayslipView getEmployeePayslipView(Long employeeId, Long payslipId) {
        Payslip p = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));

        if (!p.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Access denied for this payslip");
        }

        Employee emp = p.getEmployee();
        var org = p.getOrganization();

        LocalDate start = p.getSalaryMonth();
        LocalDate end = start.plusMonths(1).minusDays(1);
        String label = start.format(PERIOD_LABEL);

        Optional<BankAccount> empAccountOpt = bankAccountRepository.findByEmployeeId(emp.getId()).stream()
                .sorted(Comparator.comparing(BankAccount::isVerified).reversed())
                .findFirst();

        String bankName = empAccountOpt.map(BankAccount::getBankName).orElse(null);
        String masked = empAccountOpt.map(BankAccount::getAccountNumber).map(this::maskAccount).orElse(null);

        var earnings = List.of(
                EmployeePayslipView.Component.builder().name("Basic").amount(p.getBasic()).build(),
                EmployeePayslipView.Component.builder().name("HRA").amount(p.getHra()).build(),
                EmployeePayslipView.Component.builder().name("Allowances").amount(p.getAllowances()).build()
        );

        var deductions = List.of(
                EmployeePayslipView.Component.builder().name("Deductions").amount(p.getDeductions()).build()
        );

        var totals = EmployeePayslipView.Totals.builder()
                .grossEarnings(p.getBasic().add(p.getHra()).add(p.getAllowances()))
                .totalDeductions(p.getDeductions())
                .netPay(p.getNetAmount())
                .currency("INR")
                .build();

        var empBlock = EmployeePayslipView.EmployeeBlock.builder()
                .code(emp.getEmployeeCode())
                .name(emp.getFullName())
                .department(emp.getDepartment())
                .designation(emp.getDesignation())
                .build();

        var orgBlock = EmployeePayslipView.OrganizationBlock.builder()
                .name(org.getName())
                .address(org.getAddress())
                .contactNumber(org.getContactNumber())
                .supportEmail(org.getAdminUser() != null ? org.getAdminUser().getEmail() : null)
                .build();

        var payment = EmployeePayslipView.Payment.builder()
                .paidDate(end)
                .paymentRef(p.getTransactionRef())
                .bankName(bankName)
                .maskedAccount(masked)
                .build();

        return EmployeePayslipView.builder()
                .payslipId(p.getId())
                .periodLabel(label)
                .periodStart(start)
                .periodEnd(end)
                .employee(empBlock)
                .organization(orgBlock)
                .earnings(earnings)
                .deductions(deductions)
                .totals(totals)
                .payment(payment)
                .generatedAt(p.getGeneratedAt())
                .disclaimer("This is a system-generated document.")
                .build();
    }

 // Existing (non-paginated)
    @Override
    public List<PayslipListItemDTO> listPayslipsForEmployee(Long employeeId, LocalDate month) {
        List<Payslip> slips;
        if (month != null) {
            slips = payslipRepository.findByEmployeeIdAndSalaryMonth(employeeId, month);
        } else {
            slips = payslipRepository.findByEmployeeId(employeeId);
        }
        return slips.stream().map(this::toListItem).collect(Collectors.toList());
    }

    // ✅ ADD THIS (paginated)
    @Override
    public Page<PayslipListItemDTO> listPayslipsForEmployee(Long employeeId, LocalDate month, Pageable pageable) {
        Page<Payslip> slips;
        if (month != null) {
            slips = payslipRepository.findByEmployeeIdAndSalaryMonth(employeeId, month, pageable);
        } else {
            slips = payslipRepository.findByEmployeeId(employeeId, pageable);
        }
        return slips.map(this::toListItem);
    }



    @Override
    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetail(Long employeeId, Long payslipId) {
        Payslip p = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));

        if (!p.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Access denied for this payslip");
        }

        return toDetail(p);
    }

    @Override
    public void assertPayslipOwnedBy(Long payslipId, Long orgId, Long employeeId) {
        boolean exists = payslipRepository.existsByIdAndOrganizationIdAndEmployeeId(payslipId, orgId, employeeId);
        if (!exists) {
            throw new ResourceNotFoundException("Payslip not found for employee in organization");
        }
    }

    // --- Helpers ---
    private PayslipListItemDTO toListItem(Payslip p) {
        PayslipListItemDTO dto = new PayslipListItemDTO();
        dto.setPayslipId(p.getId());
        dto.setSalaryMonth(p.getSalaryMonth());
        dto.setNetAmount(p.getNetAmount());
        dto.setTransactionRef(p.getTransactionRef());
        dto.setGeneratedAt(p.getGeneratedAt());
        return dto;
    }

    private PayslipDetailDTO toDetail(Payslip p) {
        PayslipDetailDTO dto = new PayslipDetailDTO();
        dto.setPayslipId(p.getId());
        dto.setEmployeeId(p.getEmployee().getId());
        dto.setEmployeeName(p.getEmployee().getFullName());
        dto.setOrganizationId(p.getOrganization().getId());
        dto.setOrganizationName(p.getOrganization().getName());
        dto.setBatchId(p.getBatch().getId());
        dto.setLineId(p.getLine().getId());
        dto.setSalaryMonth(p.getSalaryMonth());
        dto.setBasic(p.getBasic());
        dto.setHra(p.getHra());
        dto.setAllowances(p.getAllowances());
        dto.setDeductions(p.getDeductions());
        dto.setNetAmount(p.getNetAmount());
        dto.setTransactionRef(p.getTransactionRef());
        dto.setGeneratedAt(p.getGeneratedAt());
        return dto;
    }

    private String maskAccount(String acc) {
        if (acc == null || acc.length() < 4) return "XXXX";
        return "XXXX" + acc.substring(acc.length() - 4);
    }
}

