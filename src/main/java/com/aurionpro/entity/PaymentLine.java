package com.aurionpro.entity;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_lines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentLine {

    public enum Status { QUEUED, PAID, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent batch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private PaymentBatch batch;

    // Counterparty
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    // For future client support you can add: @ManyToOne Client client;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.QUEUED;

    @Column(length = 120)
    private String transactionRef;

    @Column(length = 500)
    private String failureReason;

    private Instant processedAt;
}
