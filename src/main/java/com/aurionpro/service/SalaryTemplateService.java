package com.aurionpro.service;

import com.aurionpro.dtos.SalaryTemplateDTO;

import java.util.List;

public interface SalaryTemplateService {

    /**
     * Create or update a salary template for an organization.
     */
    SalaryTemplateDTO createOrUpdateTemplate(Long orgId, SalaryTemplateDTO templateDTO);

    /**
     * Retrieve all salary templates for a given organization.
     */
    List<SalaryTemplateDTO> getTemplatesByOrganization(Long orgId);
}
