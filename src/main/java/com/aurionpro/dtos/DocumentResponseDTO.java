package com.aurionpro.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentResponseDTO {
    private Long id;
    private String name;
    private String filename;
    private String fileType;
    private String url;
    private String verificationStatus;
    private String rejectionReason;
    private String reviewer;
}
