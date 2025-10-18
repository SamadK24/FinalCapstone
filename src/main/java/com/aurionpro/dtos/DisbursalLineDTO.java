package com.aurionpro.dtos;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisbursalLineDTO {
    private Long lineId;
    private Long employeeId;
    private String employeeName;
    private BigDecimal amount;
    private String status;
}
