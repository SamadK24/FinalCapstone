package com.aurionpro.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeBulkResultDTO {
    private int rowNumber;
    private String employeeCode;
    private boolean success;
    private String message;
}

