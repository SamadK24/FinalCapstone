package com.aurionpro.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.ConcernResponse;
import com.aurionpro.entity.Concern;
import com.aurionpro.filters.ConcernFilter;
import com.aurionpro.repository.ConcernRepository;
import com.aurionpro.service.ConcernQueryService;
import com.aurionpro.service.ConcernService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcernQueryServiceImpl implements ConcernQueryService {

    private final ConcernRepository concernRepository;
    private final ConcernService concernService;

    @Override
    @Transactional(readOnly = true)
    public Page<ConcernResponse> listForOrgFiltered(ConcernFilter filter, Pageable pageable) {
        Page<Concern> page = concernRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    private ConcernResponse toResponse(Concern c) {
        return ConcernResponse.builder()
            .id(c.getId())
            .orgId(c.getOrgId())
            .employeeId(c.getEmployeeId())
            .category(c.getCategory())
            .subject(c.getSubject())
            .description(c.getDescription())
            .status(c.getStatus())
            .attachmentRefs(c.getAttachmentRefs())
            .payslipId(c.getPayslipId())
            .batchLineId(c.getBatchLineId())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .events(java.util.List.of()) // lightweight list endpoint
            .build();
    }
}

