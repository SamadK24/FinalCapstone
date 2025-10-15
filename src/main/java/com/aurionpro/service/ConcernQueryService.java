package com.aurionpro.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.aurionpro.dtos.ConcernResponse;
import com.aurionpro.filters.ConcernFilter;

public interface ConcernQueryService {
    Page<ConcernResponse> listForOrgFiltered(ConcernFilter filter, Pageable pageable);
}
