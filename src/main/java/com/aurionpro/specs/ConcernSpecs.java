package com.aurionpro.specs;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;

import com.aurionpro.entity.Concern;
import com.aurionpro.filters.ConcernFilter;

import jakarta.persistence.criteria.Predicate;

public class ConcernSpecs {

    public static Specification<Concern> byFilter(ConcernFilter f) {
        return (root, q, cb) -> {
            var p = new ArrayList<Predicate>();
            p.add(cb.equal(root.get("orgId"), f.getOrgId()));

            if (f.getStatuses() != null && !f.getStatuses().isEmpty()) {
                var in = cb.in(root.get("status"));
                f.getStatuses().forEach(in::value);
                p.add(in);
            }

            if (f.getCategories() != null && !f.getCategories().isEmpty()) {
                var in = cb.in(root.get("category"));
                f.getCategories().forEach(in::value);
                p.add(in);
            }

            if (f.getEmployeeId() != null) {
                p.add(cb.equal(root.get("employeeId"), f.getEmployeeId()));
            }

            if (f.getSearch() != null && !f.getSearch().isBlank()) {
                String like = "%" + f.getSearch().trim().toLowerCase() + "%";
                p.add(cb.or(
                    cb.like(cb.lower(root.get("subject")), like),
                    cb.like(cb.lower(root.get("description")), like)
                ));
            }

            String df = "updatedAt";
            if ("createdAt".equalsIgnoreCase(f.getDateField())) df = "createdAt";

            if (f.getFromDate() != null) {
                Instant from = f.getFromDate().atStartOfDay(ZoneOffset.UTC).toInstant();
                p.add(cb.greaterThanOrEqualTo(root.get(df), from));
            }

            if (f.getToDate() != null) {
                Instant to = f.getToDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);
                p.add(cb.lessThanOrEqualTo(root.get(df), to));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
