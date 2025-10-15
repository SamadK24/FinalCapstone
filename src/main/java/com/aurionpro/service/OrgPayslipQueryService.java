package com.aurionpro.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aurionpro.filters.PayslipFilter;
import com.aurionpro.entity.Payslip;

public interface OrgPayslipQueryService {

    Page<PayslipService.PayslipListItemDTO> listForOrgFiltered(PayslipFilter filter, Pageable pageable);
}
