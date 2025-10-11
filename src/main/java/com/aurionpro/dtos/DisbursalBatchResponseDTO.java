package com.aurionpro.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DisbursalBatchResponseDTO {
    private Long batchId;
    private Long organizationId;
    private LocalDate salaryMonth;
    private BigDecimal totalAmount;
    private String status;
    private List<DisbursalLineDTO> lines;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DisbursalLineDTO {
        private Long lineId;
        private Long employeeId;
        private String employeeName;
        private BigDecimal amount;
        private String status;
    }
}

