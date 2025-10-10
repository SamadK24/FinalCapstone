package com.aurionpro.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class OrganizationRegistrationDTO {

    @NotBlank(message = "Organization name is mandatory")
    private String name;

    @NotBlank(message = "Contact number is mandatory")
    @Size(min = 10, max = 15)
    private String contactNumber;

    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotBlank(message = "Admin Username is mandatory")
    private String adminUsername;

    @NotBlank(message = "Admin Email is mandatory")
    @Email(message = "Admin Email should be valid")
    private String adminEmail;

    @NotBlank(message = "Admin Password is mandatory")
    @Size(min = 8, message = "Password must be minimum 8 characters")
    private String adminPassword;
}
