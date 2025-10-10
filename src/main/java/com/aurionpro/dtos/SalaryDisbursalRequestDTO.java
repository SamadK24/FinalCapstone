package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SalaryDisbursalRequestDTO {

    @NotNull
    private Long employeeId;

    @DecimalMin("0.0")
    private Double amount;

    @NotNull
    private LocalDate salaryMonth;
}
