package com.aurionpro.dtos;

import java.time.Instant;
import java.util.List;

import com.aurionpro.entity.Concern.ConcernAction;
import com.aurionpro.entity.Concern.ConcernActorRole;
import com.aurionpro.entity.Concern.ConcernCategory;
import com.aurionpro.entity.Concern.ConcernStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConcernResponse {
	private Long id;
	private Long orgId;
	private Long employeeId;
	private ConcernCategory category;
	private String subject;
	private String description;
	private ConcernStatus status;
	private List<String> attachmentRefs;
	private Long payslipId;
	private Long batchLineId;
	private Instant createdAt;
	private Instant updatedAt;
	private List<ConcernEventItem> events;

	@Getter
	@Setter
	@Builder
	public static class ConcernEventItem {
		private Long id;
		private ConcernActorRole actorRole;
		private ConcernAction action;
		private String note;
		private ConcernStatus fromStatus;
		private ConcernStatus toStatus;
		private Instant createdAt;
	}
}
