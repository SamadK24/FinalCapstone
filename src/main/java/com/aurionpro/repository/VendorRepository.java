package com.aurionpro.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aurionpro.entity.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    List<Vendor> findByOrganizationId(Long orgId);
    Optional<Vendor> findByIdAndOrganizationId(Long id, Long orgId);
}

