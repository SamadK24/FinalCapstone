package com.aurionpro.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeBulkRowDTO {
    private String fullName;
    private String email;
    private String employeeCode;
    private String dateOfJoining; // optional ISO yyyy-MM-dd
}

