package com.aurionpro.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationApprovalDTO {

    @NotNull(message = "Organization id is mandatory")
    private Long organizationId;

    @NotNull(message = "Approval decision is mandatory")
    private Boolean approve; // true = approve, false = reject

    private String rejectionReason; // optional
}
