package com.aurionpro.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Size(max = 100, message = "Organization name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Contact number is mandatory")
    @Size(min = 10, max = 10, message = "Contact number must be exactly 10 digits")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must contain only digits")
    private String contactNumber;


    @NotBlank(message = "Address is mandatory")
    @Size(max = 250, message = "Address cannot exceed 250 characters")
    private String address;

    @NotBlank(message = "Admin Username is mandatory")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String adminUsername;

    @NotBlank(message = "Admin Email is mandatory")
    @Email(message = "Admin Email should be valid")
    @Size(max = 50, message = "Admin Email cannot exceed 50 characters")
    private String adminEmail;

    @NotBlank(message = "Admin Password is mandatory")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character")
    private String adminPassword;
}

