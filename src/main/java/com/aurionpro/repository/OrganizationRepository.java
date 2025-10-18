package com.aurionpro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByName(String name);

    List<Organization> findByStatus(Organization.Status status);

    Page<Organization> findByStatus(Organization.Status status, Pageable pageable);

    Optional<Organization> findByAdminUserId(Long adminUserId);

    boolean existsByIdAndAdminUserUsername(Long orgId, String username);

    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.payrollBankAccounts WHERE o.id = :id")
    Optional<Organization> findByIdWithBankAccounts(@Param("id") Long id);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long excludeId);
}
