package com.aurionpro.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.entity.Payslip;
import com.aurionpro.filters.DateRangeResolver;
import com.aurionpro.filters.PayslipFilter;
import com.aurionpro.repository.PayslipRepository;
import com.aurionpro.service.OrgPayslipQueryService;
import com.aurionpro.service.PayslipService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrgPayslipQueryServiceImpl implements OrgPayslipQueryService {

    private final PayslipRepository payslipRepository;
    private final PayslipService payslipService;

    @Override
    @Transactional(readOnly = true)
    public Page<PayslipService.PayslipListItemDTO> listForOrgFiltered(PayslipFilter filter, Pageable pageable) {
        // Frequency -> from/to months if explicit range missing
        if ((filter.getFromMonth() == null && filter.getToMonth() == null) && filter.getFrequency() != null) {
            var fr = DateRangeResolver.fromFrequency(
                    DateRangeResolver.Frequency.valueOf(filter.getFrequency()), 
                    filter.getAnchor()
            );
            if (fr != null) {
                filter.setFromMonth(fr.from());
                filter.setToMonth(fr.to());
            }
        }

        Page<Payslip> page = payslipRepository.findAll(pageable);
        return page.map(this::toListItem);
    }

    private PayslipService.PayslipListItemDTO toListItem(Payslip p) {
        var dto = new PayslipService.PayslipListItemDTO();
        dto.setPayslipId(p.getId());
        dto.setSalaryMonth(p.getSalaryMonth());
        dto.setNetAmount(p.getNetAmount());
        dto.setTransactionRef(p.getTransactionRef());
        dto.setGeneratedAt(p.getGeneratedAt());
        return dto;
    }
}

