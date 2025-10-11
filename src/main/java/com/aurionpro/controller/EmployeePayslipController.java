package com.aurionpro.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/payslips")
    public ResponseEntity<List<PayslipListItemDTO>> listPayslips(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(name = "month", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {

        Long employeeId = principalEmployeeResolver.resolveEmployeeId(principal);
        List<PayslipListItemDTO> slips = payslipService.listPayslipsForEmployee(employeeId, month);
        return ResponseEntity.ok(slips);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/payslips/{payslipId}")
    public ResponseEntity<PayslipDetailDTO> getPayslip(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long payslipId) {

        Long employeeId = principalEmployeeResolver.resolveEmployeeId(principal);
        PayslipDetailDTO dto = payslipService.getPayslipDetail(employeeId, payslipId);
        return ResponseEntity.ok(dto);
    }
}
