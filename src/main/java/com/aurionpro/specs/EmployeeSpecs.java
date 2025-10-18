package com.aurionpro.specs;

import com.aurionpro.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecs {

	public static Specification<Employee> forOrgWithFilters(Long orgId, Employee.Status status, String search) {
	    return (root, q, cb) -> {
	        var p = new ArrayList<Predicate>();
	        p.add(cb.equal(root.get("organization").get("id"), orgId));
	        
	        // ✅ Only filter by status if explicitly provided
	        if (status != null) {
	            p.add(cb.equal(root.get("status"), status));
	        }
	        // If status is null, fetch ALL statuses (ACTIVE + INACTIVE)
	        
	        if (search != null && !search.isBlank()) {
	            String like = "%" + search.trim().toLowerCase() + "%";
	            p.add(cb.or(
	                cb.like(cb.lower(root.get("fullName")), like),
	                cb.like(cb.lower(root.get("email")), like),
	                cb.like(cb.lower(root.get("employeeCode")), like),
	                cb.like(cb.lower(root.get("department")), like),
	                cb.like(cb.lower(root.get("designation")), like)
	            ));
	        }
	        return cb.and(p.toArray(new Predicate[0]));
	    };
	}

}

