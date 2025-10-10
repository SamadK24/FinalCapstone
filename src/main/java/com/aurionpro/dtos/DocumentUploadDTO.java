package com.aurionpro.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentUploadDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String fileUrl;  // URL or identifier from cloud storage
}
