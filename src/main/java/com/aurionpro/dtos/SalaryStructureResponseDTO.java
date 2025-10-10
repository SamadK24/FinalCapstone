package com.aurionpro.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryStructureResponseDTO {

    private Long id;
    private Double basicSalary;
    private Double hra;
    private Double allowances;
    private Double deductions;
    private Long employeeId;
}
