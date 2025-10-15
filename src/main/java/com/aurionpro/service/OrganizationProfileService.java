package com.aurionpro.service;

import com.aurionpro.dtos.UpdateOrgProfileRequest;
import com.aurionpro.entity.Organization;

public interface OrganizationProfileService {

    /**
     * Update basic organization details
     */
    void updateProfile(Long orgId, UpdateOrgProfileRequest req);

    /**
     * Change admin password for the organization
     */
    void changePassword(Long orgId, String currentPassword, String newPassword, String confirm);

    /**
     * Load organization by ID
     */
    Organization getOrganizationById(Long orgId);
}
