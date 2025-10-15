package com.aurionpro.controller;

import java.time.LocalDate;
import java.util.List;

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

import com.aurionpro.dtos.ConcernResponse;
import com.aurionpro.entity.Concern;
import com.aurionpro.filters.ConcernFilter;
import com.aurionpro.service.ConcernExportService;
import com.aurionpro.service.ConcernQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/concerns-filtered")
public class OrgConcernFilterController {

	private final ConcernQueryService concernQueryService;

	private final ConcernExportService concernExportService;

	@PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
	@GetMapping
	public ResponseEntity<Page<ConcernResponse>> listFiltered(@PathVariable Long orgId,
			@RequestParam(required = false) List<Concern.ConcernStatus> status,
			@RequestParam(required = false) List<Concern.ConcernCategory> category,
			@RequestParam(required = false) Long employeeId, @RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "updatedAt") String dateField,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "updatedAt") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDir) {
		if (!java.util.Set.of("updatedAt", "createdAt", "id").contains(sortBy)) {
			throw new com.aurionpro.exceptions.BusinessRuleException("Invalid sortBy");
		}

		Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

		ConcernFilter filter = ConcernFilter.builder().orgId(orgId).statuses(status).categories(category)
				.employeeId(employeeId).search(search).dateField(dateField).fromDate(fromDate).toDate(toDate).build();

		return ResponseEntity.ok(concernQueryService.listForOrgFiltered(filter, pageable));
	}

	@PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
	@GetMapping("/export.csv")
	public ResponseEntity<byte[]> exportConcernsCsv(@PathVariable Long orgId,
			@RequestParam(required = false) List<Concern.ConcernStatus> status,
			@RequestParam(required = false) List<Concern.ConcernCategory> category,
			@RequestParam(required = false) Long employeeId, @RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "updatedAt") String dateField,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(defaultValue = "updatedAt") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDir) {
		if (!java.util.Set.of("updatedAt", "createdAt", "id").contains(sortBy)) {
			throw new com.aurionpro.exceptions.BusinessRuleException("Invalid sortBy");
		}

		ConcernFilter filter = ConcernFilter.builder().orgId(orgId).statuses(status).categories(category)
				.employeeId(employeeId).search(search).dateField(dateField).fromDate(fromDate).toDate(toDate).build();

		byte[] data = concernExportService.exportCsv(filter, sortBy, sortDir);

		String filename = "Concerns_" + orgId + "_"
				+ java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").format(java.time.LocalDateTime.now())
				+ ".csv";

		return ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + filename + "\"")
				.contentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8")).body(data);
	}

	@PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
	@GetMapping("/export.pdf")
	public ResponseEntity<byte[]> exportConcernsPdf(@PathVariable Long orgId,
			@RequestParam(required = false) List<Concern.ConcernStatus> status,
			@RequestParam(required = false) List<Concern.ConcernCategory> category,
			@RequestParam(required = false) Long employeeId, @RequestParam(required = false) String search,
			@RequestParam(required = false, defaultValue = "updatedAt") String dateField,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(defaultValue = "updatedAt") String sortBy,
			@RequestParam(defaultValue = "DESC") String sortDir) {
		if (!java.util.Set.of("updatedAt", "createdAt", "id").contains(sortBy)) {
			throw new com.aurionpro.exceptions.BusinessRuleException("Invalid sortBy");
		}

		ConcernFilter filter = ConcernFilter.builder().orgId(orgId).statuses(status).categories(category)
				.employeeId(employeeId).search(search).dateField(dateField).fromDate(fromDate).toDate(toDate).build();

		byte[] data = concernExportService.exportPdf(filter, sortBy, sortDir);

		String filename = "Concerns_" + orgId + "_"
				+ java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").format(java.time.LocalDateTime.now())
				+ ".pdf";

		return ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + filename + "\"")
				.contentType(org.springframework.http.MediaType.APPLICATION_PDF).body(data);
	}
}
