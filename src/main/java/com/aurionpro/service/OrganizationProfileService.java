package com.aurionpro.service;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.dtos.UpdateOrgProfileRequest;
import com.aurionpro.entity.Organization;
import com.aurionpro.entity.User;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.exceptions.ResourceNotFoundException;
import com.aurionpro.repository.OrganizationRepository;
import com.aurionpro.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationProfileService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final UserRepository userRepository; // injected

    @Transactional
    public void updateProfile(Long orgId, UpdateOrgProfileRequest req) {
        Organization org = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Unique name check
        if (req.getName() != null && !req.getName().equalsIgnoreCase(org.getName())) {
            boolean exists = organizationRepository.existsByNameIgnoreCaseAndIdNot(req.getName(), org.getId());
            if (exists) throw new BusinessRuleException("Organization name already in use");
            org.setName(req.getName());
        }

        if (req.getContactNumber() != null) org.setContactNumber(req.getContactNumber());
        if (req.getAddress() != null) org.setAddress(req.getAddress());

        organizationRepository.save(org);
    }

    @Transactional
    public void changePassword(Long orgId, String currentPassword, String newPassword, String confirm) {
        if (!Objects.equals(newPassword, confirm)) {
            throw new BusinessRuleException("Passwords do not match");
        }
        Organization org = organizationRepository.findById(orgId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        User admin = org.getAdminUser();
        if (admin == null) {
            throw new BusinessRuleException("No admin user for organization");
        }
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        passwordPolicy.validateNewPassword(currentPassword, newPassword);
        admin.setPassword(passwordEncoder.encode(newPassword));
        // Persist via your UserRepository if not cascaded; if Organization does not cascade User updates, inject UserRepository and save(admin)
         userRepository.save(admin);
    }
}

