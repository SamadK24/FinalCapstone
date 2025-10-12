package com.aurionpro.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aurionpro.entity.Concern.ConcernStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    public void notifyDisbursalApproval(Long employeeId, Double amount) {
        System.out.println("Notify employee " + employeeId + " of salary disbursal amount: " + amount);
        // Extend with real notification via email/SMS/Push
    }

    public void notifyDisbursalRejection(Long employeeId, String reason) {
        System.out.println("Notify employee " + employeeId + " of disbursal rejection: " + reason);
        // Extend with real notification
    }

    public void notifyOrganizationApproval(Long organizationId) {
        System.out.println("Notify organization " + organizationId + " of approval");
        // Extend with real notification
    }

    public void notifyOrganizationRejection(Long organizationId, String reason) {
        System.out.println("Notify organization " + organizationId + " of rejection: " + reason);
        // Extend with real notification
    }

    public void notifyKycApproved(Long entityId, String entityType) {
        System.out.println("Notify " + entityType + " " + entityId + " of KYC approval");
    }

    public void notifyKycRejected(Long entityId, String entityType, String reason) {
        System.out.println("Notify " + entityType + " " + entityId + " of KYC rejection: " + reason);
    }
    
    @Async // Remove if @EnableAsync not configured
    public void notifyOrgAdminsConcernCreated(Long orgId, Long concernId) {
        try {
            // mailService.sendConcernCreated(orgId, concernId);
            log.info("Concern created notification queued: orgId={}, concernId={}", orgId, concernId);
        } catch (Exception ex) {
            // Do not break the main flow
            log.warn("Failed to send concern notification: {}", ex.getMessage());
        }
    }
    public void notifyEmployeeConcernComment(Long employeeId, Long concernId) { /* log only */ }
    public void notifyEmployeeConcernResolved(Long employeeId, Long concernId, ConcernStatus status) { /* log only */ }
}
