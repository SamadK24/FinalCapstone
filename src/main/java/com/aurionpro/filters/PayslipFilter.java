package com.aurionpro.filters;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
public class PayslipFilter {
    private Long orgId;
    private Long employeeId; // optional
    private String employeeCode; // optional, contains
    private String department; // optional, equals
    private LocalDate fromMonth; // inclusive
    private LocalDate toMonth;   // inclusive
    private String frequency; // MONTHLY|QUARTERLY|YEARLY
    private String anchor;    // yyyy-MM or yyyy
    private String search;    // optional; name/code contains
}
