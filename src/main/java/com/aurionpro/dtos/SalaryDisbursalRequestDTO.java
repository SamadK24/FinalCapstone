package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SalaryDisbursalRequestDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Salary month is required")
    @PastOrPresent(message = "Salary month cannot be in the future")
    private LocalDate salaryMonth;
    
    // Amount field removed - always calculated from salary template
}
