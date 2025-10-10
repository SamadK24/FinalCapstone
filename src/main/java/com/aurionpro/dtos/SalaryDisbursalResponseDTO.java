package com.aurionpro.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SalaryDisbursalResponseDTO {

    private Long id;
    private Long organizationId;
    private Long employeeId;
    private Double amount;
    private LocalDate salaryMonth;
    private String status;
    private String rejectionReason;
}
