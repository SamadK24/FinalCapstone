package com.aurionpro.dtos;

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
public class EmployeeProfileDTO {
    private String fullName;
    private String email;
    private String employeeCode;
    private String designation;
    private String department;
    private String dateOfJoining;
    private Long salaryTemplateId;        // Add this
    private String salaryTemplateName;    // Add this
}
