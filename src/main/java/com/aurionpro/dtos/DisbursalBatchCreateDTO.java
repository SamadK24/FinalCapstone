package com.aurionpro.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisbursalBatchCreateDTO {
    // If employeeIds is empty or null, compute for all eligible org employees
    private List<Long> employeeIds;
    private LocalDate salaryMonth;
}
