package com.aurionpro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.Organization;

import ch.qos.logback.core.status.Status;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByName(String name);
    List<Organization> findByStatus(Organization.Status status);
    Optional<Organization> findByAdminUserId(Long adminUserId);
    

}
