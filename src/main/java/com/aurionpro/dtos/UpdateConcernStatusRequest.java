package com.aurionpro.dtos;

import com.aurionpro.entity.Concern.ConcernStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateConcernStatusRequest {
    @NotNull
    private ConcernStatus toStatus;

    @Size(min=5, max=1000)
    private String note;
}

