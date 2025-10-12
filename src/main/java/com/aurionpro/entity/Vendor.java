package com.aurionpro.entity;

import com.aurionpro.entity.BankAccount.KYCDocumentVerificationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendors",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"organization_id", "name"})
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vendor {

    public enum Status { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tenant ownership
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 160)
    private String contactEmail;

    @Column(length = 30)
    private String contactPhone;

    // Destination details (external bank destination; not a balance ledger)
    @Column(length = 140)
    private String accountHolderName;

    @Column(length = 64)
    private String accountNumber;

    @Column(length = 20)
    private String ifscCode;

    @Column(length = 80)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KYCDocumentVerificationStatus kycStatus = KYCDocumentVerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    // Optional Cloudinary document public IDs (comma-separated or moved to a separate table if needed)
    @Column(length = 512)
    private String documentRefs;
    
 // new fields
    private java.time.Instant lastKycReviewedAt;

    @jakarta.persistence.Column(length = 100)
    private String lastKycReviewedBy;

    @jakarta.persistence.Column(length = 500)
    private String kycReviewReason;

    // safety defaults for builder and any path that leaves enums null
    @jakarta.persistence.PrePersist
    public void prePersistDefaults() {
        if (this.kycStatus == null) {
            this.kycStatus = com.aurionpro.entity.BankAccount.KYCDocumentVerificationStatus.PENDING;
        }
        if (this.status == null) {
            this.status = Status.ACTIVE;
        }
    }


}

