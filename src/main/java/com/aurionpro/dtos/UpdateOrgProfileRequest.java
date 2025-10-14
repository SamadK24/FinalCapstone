package com.aurionpro.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class UpdateOrgProfileRequest {
    @NotBlank @Size(min = 3, max = 100)
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid contact number")
    @Size(max = 15)
    private String contactNumber;

    @Size(max = 255)
    private String address;
}
