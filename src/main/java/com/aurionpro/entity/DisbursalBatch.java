package com.aurionpro.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(mappedBy = "batch", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DisbursalLine> lines = new ArrayList<>();


    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

