package com.aurionpro.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VendorKycUpdateDTO {
    @NotBlank
    private String status; // "VERIFIED" or "REJECTED"
    private String reason; // optional
}

