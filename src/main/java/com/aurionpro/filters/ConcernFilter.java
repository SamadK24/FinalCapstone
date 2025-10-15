package com.aurionpro.filters;

import java.time.LocalDate;
import java.util.List;
import com.aurionpro.entity.Concern;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
public class ConcernFilter {
    private Long orgId;
    private List<Concern.ConcernStatus> statuses; // optional
    private List<Concern.ConcernCategory> categories; // optional
    private Long employeeId; // optional
    private String search; // subject/description contains, case-insensitive
    private String dateField; // "createdAt" | "updatedAt", default: updatedAt
    private LocalDate fromDate; // inclusive
    private LocalDate toDate;   // inclusive
}
