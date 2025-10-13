package com.aurionpro.entity;

import java.time.Instant;

import com.aurionpro.entity.Concern.ConcernAction;
import com.aurionpro.entity.Concern.ConcernActorRole;
import com.aurionpro.entity.Concern.ConcernStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "concern_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcernEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long concernId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ConcernActorRole actorRole;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ConcernAction action;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private ConcernStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private ConcernStatus toStatus;

	@Column(length = 1000)
	private String note;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	public void prePersist() {
		createdAt = Instant.now();
	}

	
}
