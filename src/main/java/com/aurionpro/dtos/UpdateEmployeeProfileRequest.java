package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class UpdateEmployeeProfileRequest {

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
    @Size(max = 15)
    private String phone;

    @Email @Size(max = 100)
    private String altEmail;

    @Size(max = 255)
    private String address;
}
