package com.aurionpro.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aurionpro.entity.Payslip;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayslipService {

    private final PayslipRepository payslipRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<PayslipListItemDTO> listPayslipsForEmployee(Long employeeId, LocalDate month) {
        List<Payslip> slips = (month == null)
                ? payslipRepository.findByEmployeeId(employeeId)
                : payslipRepository.findByEmployeeIdAndSalaryMonth(employeeId, month);

        return slips.stream().map(this::toListItem).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayslipDetailDTO getPayslipDetail(Long employeeId, Long payslipId) {
        Payslip p = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip not found"));

        if (!p.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Access denied for this payslip");
        }
        return toDetail(p);
    }

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

    // DTOs (inner or separate files)
    @lombok.Getter @lombok.Setter
    public static class PayslipListItemDTO {
        private Long payslipId;
        private java.time.LocalDate salaryMonth;
        private java.math.BigDecimal netAmount;
        private String transactionRef;
        private java.time.Instant generatedAt;
    }

    @lombok.Getter @lombok.Setter
    public static class PayslipDetailDTO {
        private Long payslipId;
        private Long employeeId;
        private String employeeName;
        private Long organizationId;
        private String organizationName;
        private Long batchId;
        private Long lineId;
        private java.time.LocalDate salaryMonth;
        private java.math.BigDecimal basic;
        private java.math.BigDecimal hra;
        private java.math.BigDecimal allowances;
        private java.math.BigDecimal deductions;
        private java.math.BigDecimal netAmount;
        private String transactionRef;
        private java.time.Instant generatedAt;
    }
    
    public void assertPayslipOwnedBy(Long payslipId, Long orgId, Long employeeId) {
        boolean exists = payslipRepository.existsByIdAndOrganizationIdAndEmployeeId(payslipId, orgId, employeeId);
        if (!exists) {
            throw new ResourceNotFoundException("Payslip not found for employee in organization");
        }
    }
}

