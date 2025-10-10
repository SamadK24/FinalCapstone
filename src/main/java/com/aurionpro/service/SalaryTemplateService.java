package com.aurionpro.service;

import com.aurionpro.dtos.SalaryTemplateDTO;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.SalaryTemplate;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.SalaryTemplateRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryTemplateService {

    private final SalaryTemplateRepository salaryTemplateRepository;
    private final OrganizationRepository organizationRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public SalaryTemplateDTO createOrUpdateTemplate(Long orgId, SalaryTemplateDTO templateDTO) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        SalaryTemplate template = modelMapper.map(templateDTO, SalaryTemplate.class);
        template.setOrganization(org);

        SalaryTemplate saved = salaryTemplateRepository.save(template);
        return modelMapper.map(saved, SalaryTemplateDTO.class);
    }

    public List<SalaryTemplateDTO> getTemplatesByOrganization(Long orgId) {
        List<SalaryTemplate> templates = salaryTemplateRepository.findByOrganizationId(orgId);
        return templates.stream()
                .map(t -> modelMapper.map(t, SalaryTemplateDTO.class))
                .collect(Collectors.toList());
    }
}
