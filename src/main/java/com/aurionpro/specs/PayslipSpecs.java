package com.aurionpro.specs;

import java.util.ArrayList;

import org.springframework.data.jpa.domain.Specification;

import com.aurionpro.entity.Payslip;
import com.aurionpro.filters.PayslipFilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class PayslipSpecs {

    public static Specification<Payslip> byFilter(PayslipFilter f) {
        return (root, q, cb) -> {
            var p = new ArrayList<Predicate>();
            p.add(cb.equal(root.get("organization").get("id"), f.getOrgId()));

            Join<Object, Object> emp = root.join("employee");

            if (f.getEmployeeId() != null) {
                p.add(cb.equal(emp.get("id"), f.getEmployeeId()));
            }

            if (f.getEmployeeCode() != null && !f.getEmployeeCode().isBlank()) {
                String like = "%" + f.getEmployeeCode().trim().toLowerCase() + "%";
                p.add(cb.like(cb.lower(emp.get("employeeCode")), like));
            }

            if (f.getDepartment() != null && !f.getDepartment().isBlank()) {
                p.add(cb.equal(emp.get("department"), f.getDepartment()));
            }

            if (f.getFromMonth() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("salaryMonth"), f.getFromMonth()));
            }

            if (f.getToMonth() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("salaryMonth"), f.getToMonth()));
            }

            if (f.getSearch() != null && !f.getSearch().isBlank()) {
                String like = "%" + f.getSearch().trim().toLowerCase() + "%";
                p.add(cb.or(
                    cb.like(cb.lower(emp.get("fullName")), like),
                    cb.like(cb.lower(emp.get("employeeCode")), like)
                ));
            }

            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}

