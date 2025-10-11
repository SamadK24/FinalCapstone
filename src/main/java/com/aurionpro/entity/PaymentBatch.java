package com.aurionpro.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_batches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentBatch {

    public enum Type { VENDOR, CLIENT }
    public enum Status { PENDING, APPROVED, REJECTED, COMPLETED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Scope
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type = Type.VENDOR;

    // Optional logical period or date
    @Column(nullable = false)
    private LocalDate paymentDate;

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
    private Set<PaymentLine> lines;

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); }
}

