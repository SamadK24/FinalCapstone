package com.aurionpro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurionpro.dtos.ChangePasswordRequest;
import com.aurionpro.dtos.UpdateOrgProfileRequest;
import com.aurionpro.service.OrganizationProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization/{orgId}/profile")
public class OrganizationProfileController {

    private final OrganizationProfileService organizationProfileService;

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PatchMapping
    public ResponseEntity<Void> update(
        @PathVariable Long orgId,
        @Valid @RequestBody UpdateOrgProfileRequest req
    ) {
        organizationProfileService.updateProfile(orgId, req);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long orgId,
        @Valid @RequestBody ChangePasswordRequest req
    ) {
        organizationProfileService.changePassword(orgId, req.getCurrentPassword(), req.getNewPassword(), req.getConfirmNewPassword());
        return ResponseEntity.ok().build();
    }
}

