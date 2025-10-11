package com.aurionpro.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisbursalBatchApprovalDTO {
    @NotNull
    private Long batchId;
    @NotNull
    private Boolean approve;
    private String rejectionReason;
    private String reviewer; // @AuthenticationPrincipal username can be passed in controller
}

