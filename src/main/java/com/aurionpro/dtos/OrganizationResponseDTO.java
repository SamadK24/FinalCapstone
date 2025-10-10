package com.aurionpro.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationResponseDTO {
    private Long id;
    private String name;
    private String contactNumber;
    private String address;
    private String status;
}
