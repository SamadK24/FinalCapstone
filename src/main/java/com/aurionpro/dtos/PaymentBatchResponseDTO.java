package com.aurionpro.dtos;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class PaymentBatchResponseDTO {

    private Long batchId;
    private Long organizationId;
    private String type;
    private LocalDate paymentDate;
    private java.math.BigDecimal totalAmount;
    private String status;
    private java.util.List<PaymentLineDTO> lines;

    @Getter @Setter @Builder
    public static class PaymentLineDTO {
        private Long lineId;
        private Long vendorId;
        private String vendorName;
        private java.math.BigDecimal amount;
        private String status;
    }
}
