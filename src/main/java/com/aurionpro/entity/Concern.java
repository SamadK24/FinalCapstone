package com.aurionpro.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "concerns")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Concern {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long orgId;

	@Column(nullable = false)
	private Long employeeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ConcernCategory category;

	@Column(length = 120)
	private String subject;

	@Column(nullable = false, length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ConcernStatus status;

	@ElementCollection
	@CollectionTable(name = "concern_attachments", joinColumns = @JoinColumn(name = "concern_id"))
	@Column(name = "ref", length = 300)
	private List<String> attachmentRefs = new ArrayList<>();

	private Long payslipId;
	private Long batchLineId;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	private String createdBy;
	private String updatedBy;
	


	@PrePersist
	public void prePersist() {
		if (status == null)
			status = ConcernStatus.OPEN;
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
		if (attachmentRefs == null)
			attachmentRefs = new ArrayList<>();
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = Instant.now();
	}

	public enum ConcernStatus {
		OPEN, IN_PROGRESS, RESOLVED, CLOSED
	}

	public enum ConcernAction {
		CREATED, COMMENTED, STATUS_CHANGED, ATTACHMENT_ADDED,ASSIGNED
	}

	public enum ConcernActorRole {
		EMPLOYEE, ORGANIZATION_ADMIN
	}
	
	public enum ConcernCategory {
		PAYSLIP, SALARY, PAYMENT_STATUS, GENERAL
	}
}
