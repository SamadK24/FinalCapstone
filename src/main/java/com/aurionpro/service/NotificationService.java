package com.aurionpro.service;

import org.springframework.stereotype.Service;

@Service
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
}
