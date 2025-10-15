package com.aurionpro.filters;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;

public class DateRangeResolver {

    public record DateRange(LocalDate from, LocalDate to) {}

    public enum Frequency { MONTHLY, QUARTERLY, YEARLY }

    public static DateRange of(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        LocalDate f = from;
        LocalDate t = to;
        if (f != null && t != null && t.isBefore(f)) {
            // swap to be safe
            LocalDate tmp = f; f = t; t = tmp;
        }
        return new DateRange(f, t);
    }

    // Derive range from frequency and optional anchor month (yyyy-MM) or year (yyyy)
    public static DateRange fromFrequency(Frequency freq, String anchor) {
        if (freq == null) return null;

        switch (freq) {
            case MONTHLY -> {
                YearMonth ym = anchor != null ? YearMonth.parse(anchor) : YearMonth.now();
                LocalDate start = ym.atDay(1);
                LocalDate end = ym.atEndOfMonth();
                return new DateRange(start, end);
            }
            case QUARTERLY -> {
                YearMonth ym = anchor != null ? YearMonth.parse(anchor) : YearMonth.now();
                int q = (ym.getMonthValue() - 1) / 3; // 0..3
                int startMonth = q * 3 + 1;
                YearMonth qStart = YearMonth.of(ym.getYear(), startMonth);
                YearMonth qEnd = qStart.plusMonths(2);
                return new DateRange(qStart.atDay(1), qEnd.atEndOfMonth());
            }
            case YEARLY -> {
                Year y = anchor != null && anchor.length() == 4 ? Year.parse(anchor) : Year.now();
                return new DateRange(y.atDay(1), y.atMonth(12).atEndOfMonth());
            }
            default -> { return null; }
        }
    }
}

