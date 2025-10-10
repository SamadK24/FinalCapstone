package com.aurionpro.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentReviewDTO {

    @NotNull
    private boolean approve;

    private String rejectionReason;
}
