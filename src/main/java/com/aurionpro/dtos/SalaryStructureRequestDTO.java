package com.aurionpro.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryStructureRequestDTO {

    @NotNull
    @Min(0)
    private Double basicSalary;

    @NotNull
    @Min(0)
    private Double hra;

    @NotNull
    @Min(0)
    private Double allowances;

    @NotNull
    @Min(0)
    private Double deductions;
}
