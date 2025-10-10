package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryDisbursalApprovalDTO {

    @NotNull
    private Long disbursalRequestId;

    @NotNull
    private Boolean approve;

    private String rejectionReason; // required if approve == false
}
