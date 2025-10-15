package com.aurionpro.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurionpro.repository.OrganizationRepository;

@Service("securityService")
public class SecurityService {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public SecurityService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Checks whether the given username is the admin of the given organization.
     *
     * @param orgId the organization ID being accessed
     * @param username the username of the currently authenticated user
     * @return true if the user is the organization's admin; false otherwise
     */
    public boolean isOrgAdmin(Long orgId, String username) {
        return organizationRepository.existsByIdAndAdminUserUsername(orgId, username);
    }
}