package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeSalaryOverrideDTO {

    @NotNull
    private Long employeeId;

    private Double overrideBasicSalary;
    private Double overrideHra;
    private Double overrideAllowances;
    private Double overrideDeductions;
}
