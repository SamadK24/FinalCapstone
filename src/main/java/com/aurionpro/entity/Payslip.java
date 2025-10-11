package com.aurionpro.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payslips",
       indexes = {
           @Index(name = "idx_payslip_employee_month", columnList = "employee_id,salaryMonth")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ownership and references
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private DisbursalBatch batch;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false, unique = true)
    private DisbursalLine line;

    @Column(nullable = false)
    private LocalDate salaryMonth;

    // Frozen component snapshot for audit stability
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basic;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal hra;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal allowances;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal deductions;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;

    // Reference of the underlying “credit” execution
    @Column(length = 120, nullable = false)
    private String transactionRef;

    @Column(nullable = false)
    private Instant generatedAt;

    @PrePersist
    void onCreate() {
        this.generatedAt = Instant.now();
    }
}

