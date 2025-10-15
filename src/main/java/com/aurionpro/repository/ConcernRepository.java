package com.aurionpro.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.aurionpro.entity.Concern;
import com.aurionpro.entity.Concern.ConcernStatus;

public interface ConcernRepository extends JpaRepository<Concern, Long>,JpaSpecificationExecutor<Concern> {
	Page<Concern> findByOrgIdAndEmployeeIdAndStatusIn(Long orgId, Long employeeId,
			java.util.List<ConcernStatus> statuses, Pageable pageable);

	Page<Concern> findByOrgId(Long orgId, Pageable pageable);

	Page<Concern> findByOrgIdAndStatusIn(Long orgId, java.util.List<ConcernStatus> statuses, Pageable pageable);

	java.util.Optional<Concern> findByIdAndOrgId(Long id, Long orgId);

	java.util.Optional<Concern> findByIdAndOrgIdAndEmployeeId(Long id, Long orgId, Long employeeId);
}
