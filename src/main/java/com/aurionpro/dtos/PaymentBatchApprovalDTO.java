package com.aurionpro.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentBatchApprovalDTO {

    @NotNull private Long batchId;
    @NotNull private Boolean approve;
    private String rejectionReason;
    private String reviewer;
}
