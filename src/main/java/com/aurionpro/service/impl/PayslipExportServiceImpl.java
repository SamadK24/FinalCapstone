package com.aurionpro.service.impl;

import com.aurionpro.entity.Payslip;
import com.aurionpro.filters.DateRangeResolver;
import com.aurionpro.filters.PayslipFilter;
import com.aurionpro.repository.PayslipRepository;
import com.aurionpro.specs.PayslipSpecs;
import com.aurionpro.service.PayslipExportService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PayslipExportServiceImpl implements PayslipExportService {

    private static final int EXPORT_LIMIT = 10_000;
    private static final DateTimeFormatter YM = DateTimeFormatter.ISO_LOCAL_DATE; // salaryMonth as yyyy-MM-dd
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_INSTANT;

    private final PayslipRepository payslipRepository;

    @Override
    public byte[] exportCsv(PayslipFilter filter, String sortBy, String sortDir) {
        // Frequency -> derive months if from/to absent
        if ((filter.getFromMonth() == null && filter.getToMonth() == null) && filter.getFrequency() != null) {
            var fr = DateRangeResolver.fromFrequency(
                    DateRangeResolver.Frequency.valueOf(filter.getFrequency()),
                    filter.getAnchor());
            if (fr != null) {
                filter.setFromMonth(fr.from());
                filter.setToMonth(fr.to());
            }
        }

        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(0, EXPORT_LIMIT, Sort.by(dir, sortBy));
        var page = payslipRepository.findAll(PayslipSpecs.byFilter(filter), pageable);

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff'); // BOM
        sb.append("# GeneratedAt=").append(java.time.Instant.now())
          .append(", OrgId=").append(filter.getOrgId())
          .append(", Sort=").append(sortBy).append(" ").append(dir).append("\n");

        sb.append("payslipId,employeeCode,employeeName,department,salaryMonth,netAmount,generatedAt,transactionRef\n");
        for (Payslip p : page.getContent()) {
            var emp = p.getEmployee();
            sb.append(csv(p.getId()))
              .append(',').append(csv(emp != null ? emp.getEmployeeCode() : ""))
              .append(',').append(csv(emp != null ? emp.getFullName() : ""))
              .append(',').append(csv(emp != null ? emp.getDepartment() : ""))
              .append(',').append(csv(p.getSalaryMonth() != null ? YM.format(p.getSalaryMonth()) : ""))
              .append(',').append(csv(p.getNetAmount()))
              .append(',').append(csv(p.getGeneratedAt() != null ? TS.format(p.getGeneratedAt()) : ""))
              .append(',').append(csv(p.getTransactionRef()))
              .append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csv(Object o) {
        String s = o == null ? "" : o.toString();
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}

