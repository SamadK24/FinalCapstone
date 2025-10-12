package com.aurionpro.dtos;

import java.time.Instant;

public record AttachmentResponse(
        String url,
        String contentType,
        long size,
        Instant uploadedAt
) {}
