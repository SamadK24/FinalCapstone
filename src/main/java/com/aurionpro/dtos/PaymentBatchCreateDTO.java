package com.aurionpro.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaymentBatchCreateDTO {

    @NotNull private LocalDate paymentDate;
    @NotNull private List<Line> lines; // vendorId + amount

    @Getter @Setter
    public static class Line {
        @NotNull private Long vendorId;
        @NotNull private BigDecimal amount;
    }
}
