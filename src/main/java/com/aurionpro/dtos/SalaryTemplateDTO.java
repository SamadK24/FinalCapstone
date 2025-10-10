package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryTemplateDTO {
    private Long id;

    @NotBlank
    private String templateName;

    @NotNull @Min(0)
    private Double basicSalary;

    @NotNull @Min(0)
    private Double hra;

    @NotNull @Min(0)
    private Double allowances;

    @NotNull @Min(0)
    private Double deductions;
}
