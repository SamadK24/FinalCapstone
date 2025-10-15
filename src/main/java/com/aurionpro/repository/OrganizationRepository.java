package com.aurionpro.repository;

import com.aurionpro.entity.Organization;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
    List<Organization> findByStatus(Organization.Status status);
    Optional<Organization> findByAdminUserId(Long adminUserId);
    boolean existsByIdAndAdminUserUsername(Long orgId, String username);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long excludeId);
}

