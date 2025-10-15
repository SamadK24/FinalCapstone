package com.aurionpro.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.filters.PayslipFilter;
import com.aurionpro.service.OrgPayslipQueryService;
import com.aurionpro.service.PayslipExportService;
import com.aurionpro.service.PayslipService.PayslipListItemDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/payslips")
public class OrgPayslipController {

    private final OrgPayslipQueryService orgPayslipQueryService;
    private final PayslipExportService payslipExportService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PayslipListItemDTO>> list(
        @PathVariable Long orgId,
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) String employeeCode,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromMonth,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toMonth,
        @RequestParam(required = false) String frequency,
        @RequestParam(required = false) String anchor,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "salaryMonth") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        if (!java.util.Set.of("salaryMonth", "generatedAt", "id").contains(sortBy)) {
            throw new com.aurionpro.exceptions.BusinessRuleException("Invalid sortBy");
        }

        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

        PayslipFilter filter = PayslipFilter.builder()
            .orgId(orgId)
            .employeeId(employeeId)
            .employeeCode(employeeCode)
            .department(department)
            .fromMonth(fromMonth)
            .toMonth(toMonth)
            .frequency(frequency)
            .anchor(anchor)
            .search(search)
            .build();

        return ResponseEntity.ok(orgPayslipQueryService.listForOrgFiltered(filter, pageable));
    }
    
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportPayslipsCsv(
        @PathVariable Long orgId,
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) String employeeCode,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromMonth,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toMonth,
        @RequestParam(required = false) String frequency,
        @RequestParam(required = false) String anchor,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "salaryMonth") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        if (!java.util.Set.of("salaryMonth", "generatedAt", "id").contains(sortBy)) {
            throw new com.aurionpro.exceptions.BusinessRuleException("Invalid sortBy");
        }

        PayslipFilter filter = PayslipFilter.builder()
            .orgId(orgId)
            .employeeId(employeeId)
            .employeeCode(employeeCode)
            .department(department)
            .fromMonth(fromMonth)
            .toMonth(toMonth)
            .frequency(frequency)
            .anchor(anchor)
            .search(search)
            .build();

        byte[] data = payslipExportService.exportCsv(filter, sortBy, sortDir);

        String filename = "Payslips_" + orgId + "_" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").format(java.time.LocalDateTime.now()) + ".csv";

        return ResponseEntity.ok()
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(data);
    }

}

