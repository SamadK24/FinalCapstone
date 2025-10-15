package com.aurionpro.service;

import com.aurionpro.service.PayslipService.PayslipDetailDTO;
import com.aurionpro.service.PayslipService.PayslipListItemDTO;
import com.aurionpro.dtos.EmployeePayslipView;

import java.time.LocalDate;
import java.util.List;

public interface PayslipService {

    /**
     * Fetch a detailed EmployeePayslipView for PDF/Display
     */
    EmployeePayslipView getEmployeePayslipView(Long employeeId, Long payslipId);

    /**
     * List payslips for an employee, optionally filtered by month
     */
    List<PayslipListItemDTO> listPayslipsForEmployee(Long employeeId, LocalDate month);

    /**
     * Fetch detailed payslip information for UI
     */
    PayslipDetailDTO getPayslipDetail(Long employeeId, Long payslipId);

    /**
     * Validate that a payslip exists and belongs to the employee/org
     */
    void assertPayslipOwnedBy(Long payslipId, Long orgId, Long employeeId);

    // DTOs can be inner classes or separate files
    class PayslipListItemDTO {
        private Long payslipId;
        private LocalDate salaryMonth;
        private java.math.BigDecimal netAmount;
        private String transactionRef;
        private java.time.Instant generatedAt;

        // getters and setters
        public Long getPayslipId() { return payslipId; }
        public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }
        public LocalDate getSalaryMonth() { return salaryMonth; }
        public void setSalaryMonth(LocalDate salaryMonth) { this.salaryMonth = salaryMonth; }
        public java.math.BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(java.math.BigDecimal netAmount) { this.netAmount = netAmount; }
        public String getTransactionRef() { return transactionRef; }
        public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
        public java.time.Instant getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(java.time.Instant generatedAt) { this.generatedAt = generatedAt; }
    }

    class PayslipDetailDTO {
        private Long payslipId;
        private Long employeeId;
        private String employeeName;
        private Long organizationId;
        private String organizationName;
        private Long batchId;
        private Long lineId;
        private LocalDate salaryMonth;
        private java.math.BigDecimal basic;
        private java.math.BigDecimal hra;
        private java.math.BigDecimal allowances;
        private java.math.BigDecimal deductions;
        private java.math.BigDecimal netAmount;
        private String transactionRef;
        private java.time.Instant generatedAt;

        // getters and setters
        public Long getPayslipId() { return payslipId; }
        public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public Long getBatchId() { return batchId; }
        public void setBatchId(Long batchId) { this.batchId = batchId; }
        public Long getLineId() { return lineId; }
        public void setLineId(Long lineId) { this.lineId = lineId; }
        public LocalDate getSalaryMonth() { return salaryMonth; }
        public void setSalaryMonth(LocalDate salaryMonth) { this.salaryMonth = salaryMonth; }
        public java.math.BigDecimal getBasic() { return basic; }
        public void setBasic(java.math.BigDecimal basic) { this.basic = basic; }
        public java.math.BigDecimal getHra() { return hra; }
        public void setHra(java.math.BigDecimal hra) { this.hra = hra; }
        public java.math.BigDecimal getAllowances() { return allowances; }
        public void setAllowances(java.math.BigDecimal allowances) { this.allowances = allowances; }
        public java.math.BigDecimal getDeductions() { return deductions; }
        public void setDeductions(java.math.BigDecimal deductions) { this.deductions = deductions; }
        public java.math.BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(java.math.BigDecimal netAmount) { this.netAmount = netAmount; }
        public String getTransactionRef() { return transactionRef; }
        public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
        public java.time.Instant getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(java.time.Instant generatedAt) { this.generatedAt = generatedAt; }
    }
}
