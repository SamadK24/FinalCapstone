package com.aurionpro.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VendorUpdateDTO {

    @NotBlank private String name;
    private String contactEmail;
    private String contactPhone;
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    private String documentRefs;
    private String status; // ACTIVE/INACTIVE
}
