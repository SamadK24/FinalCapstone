package com.aurionpro.dtos;

import com.aurionpro.entity.Employee;
import java.time.LocalDate;
import lombok.*;

@Getter @Setter @Builder
public class EmployeeResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String employeeCode;
    private LocalDate dateOfJoining;
    private String designation;
    private String department;
    private String phone;
    private String altEmail;
    private String address;
    private Employee.Status status;
}
