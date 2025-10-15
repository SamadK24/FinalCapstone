package com.aurionpro.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.EmployeePayslipView;
import com.aurionpro.service.PayslipPdfService;
import com.aurionpro.service.PayslipService;
import com.aurionpro.service.PayslipService.PayslipDetailDTO;
import com.aurionpro.service.PayslipService.PayslipListItemDTO;
import com.aurionpro.service.PrincipalEmployeeResolver;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Validated
public class EmployeePayslipController {

	private final PayslipService payslipService;
	private final PrincipalEmployeeResolver principalEmployeeResolver;
	private final PayslipPdfService payslipPdfService;

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/payslips")
	public ResponseEntity<List<PayslipListItemDTO>> listPayslips(@AuthenticationPrincipal UserDetails principal,
			@RequestParam(name = "month", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {

		Long employeeId = principalEmployeeResolver.resolveEmployeeId(principal);
		List<PayslipListItemDTO> slips = payslipService.listPayslipsForEmployee(employeeId, month);
		return ResponseEntity.ok(slips);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/payslips/{payslipId}")
	public ResponseEntity<PayslipDetailDTO> getPayslip(@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long payslipId) {

		Long employeeId = principalEmployeeResolver.resolveEmployeeId(principal);
		PayslipDetailDTO dto = payslipService.getPayslipDetail(employeeId, payslipId);
		return ResponseEntity.ok(dto);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/payslips/{payslipId}/view")
	public ResponseEntity<EmployeePayslipView> getPayslipView(@AuthenticationPrincipal UserDetails principal,
			@PathVariable Long payslipId) {

		Long employeeId = principalEmployeeResolver.resolveEmployeeId(principal);
		EmployeePayslipView view = payslipService.getEmployeePayslipView(employeeId, payslipId);
		return ResponseEntity.ok(view);
	}

	@PreAuthorize("hasRole('EMPLOYEE')")
	@GetMapping("/payslips/{payslipId}/pdf")
	public ResponseEntity<byte[]> downloadPayslipPdf(
	        @AuthenticationPrincipal UserDetails principal,
	        @PathVariable Long payslipId) {

	    Long employeeId = principalEmployeeResolver.resolveEmployeeId(principal);
	    EmployeePayslipView view = payslipService.getEmployeePayslipView(employeeId, payslipId);

	    byte[] pdf = payslipPdfService.render(view);

	    // Sanitize employee code and period label safely
	    String safeCode = sanitizeForFilename(view.getEmployee().getCode(), "EMP");
	    String safePeriod = sanitizeForFilename(view.getPeriodLabel(), "PERIOD");

	    String filename = safeCode + "_" + safePeriod + "_Payslip.pdf";

	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(pdf);
	}

	/**
	 * Sanitizes a string to be safe for filenames:
	 * - Allows letters, numbers, underscore, dash
	 * - Converts spaces to underscores
	 * - Uses a default fallback if input is null/empty
	 */
	private String sanitizeForFilename(String input, String fallback) {
	    if (input == null || input.isBlank()) {
	        return fallback;
	    }
	    // Keep letters, digits, underscore, dash; replace spaces with underscores
	    return input.replaceAll("[^A-Za-z0-9_\\- ]", "")
	                .replace(' ', '_');
	}

}
