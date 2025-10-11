package com.aurionpro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "disbursal_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursalBatch {

    public enum Status {
        PENDING, APPROVED, REJECTED, COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Organization owning the payroll batch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    // Salary month for which this batch is created
    @Column(nullable = false)
    private LocalDate salaryMonth;

    // Aggregate total amount across lines (BigDecimal for money)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 100)
    private String createdBy;

    private Instant createdAt;

    @Column(length = 100)
    private String approvedBy;

    private Instant approvedAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DisbursalLine> lines;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

