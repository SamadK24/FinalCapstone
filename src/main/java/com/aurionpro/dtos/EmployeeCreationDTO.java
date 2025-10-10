package com.aurionpro.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCreationDTO {

    @NotBlank(message = "Employee Full Name is mandatory")
    private String fullName;

    @NotBlank(message = "Employee Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Employee Code is mandatory")
    private String employeeCode;
}
