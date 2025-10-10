package com.aurionpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.entity.SalaryTemplate;

import java.util.List;

@Repository
public interface SalaryTemplateRepository extends JpaRepository<SalaryTemplate, Long> {
    List<SalaryTemplate> findByOrganizationId(Long orgId);
}
