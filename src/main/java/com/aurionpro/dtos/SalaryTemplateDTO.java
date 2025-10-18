package com.aurionpro.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
