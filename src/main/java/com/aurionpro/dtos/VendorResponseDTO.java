package com.aurionpro.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class VendorResponseDTO {

    private Long id;
    private Long organizationId;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
    private String bankName;
    private String kycStatus;
    private String status;
    private String documentRefs;
}
