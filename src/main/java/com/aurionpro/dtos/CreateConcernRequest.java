package com.aurionpro.dtos;

import java.util.List;

import com.aurionpro.entity.Concern.ConcernCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateConcernRequest {
	@NotNull
	private ConcernCategory category;
	@Size(min = 5, max = 120)
	private String subject;
	@NotBlank
	@Size(min = 10, max = 2000)
	private String description;
	@Size(max = 5)
	private List<@Size(max = 300) String> attachmentRefs;
	private Long payslipId;
	private Long batchLineId;
}
