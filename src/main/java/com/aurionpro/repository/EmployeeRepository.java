package com.aurionpro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	 Optional<Employee> findByUserAccountId(Long userId);
}
