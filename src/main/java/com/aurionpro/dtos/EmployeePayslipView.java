package com.aurionpro.dtos;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
public class EmployeePayslipView {

	private Long payslipId;

	// Period
	private String periodLabel; // e.g., "Aug 2024"
	private LocalDate periodStart; // salaryMonth
	private LocalDate periodEnd; // last day of month

	// Employee block
	private EmployeeBlock employee;

	@Getter
	@Setter
	@Builder
	public static class EmployeeBlock {
		private String code;
		private String name;
		private String department;
		private String designation;
	}

	// Organization block
	private OrganizationBlock organization;

	@Getter
	@Setter
	@Builder
	public static class OrganizationBlock {
		private String name;
		private String address;
		private String contactNumber;
		private String supportEmail; // optional
	}

	// Components
	private List<Component> earnings; // Basic, HRA, Allowances
	private List<Component> deductions; // Aggregated single line for now

	@Getter
	@Setter
	@Builder
	public static class Component {
		private String name;
		private BigDecimal amount;
	}

	// Totals
	private Totals totals;

	@Getter
	@Setter
	@Builder
	public static class Totals {
		private BigDecimal grossEarnings;
		private BigDecimal totalDeductions;
		private BigDecimal netPay;
		private String currency; // INR
	}

	// Payment
	private Payment payment;

	@Getter
	@Setter
	@Builder
	public static class Payment {
		private LocalDate paidDate; // periodEnd for now
		private String paymentRef; // transactionRef
		private String bankName; // from BankAccount if available
		private String maskedAccount; // XXXX1234, if available
	}

	// Metadata
	private Instant generatedAt;
	private String disclaimer; // "This is a system-generated document."
}
