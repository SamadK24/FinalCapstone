package com.aurionpro.service;

public interface EmailService {
    void sendOrganizationRegistered(String toEmail, String orgName, String adminUsername);
    void sendOrganizationApproved(String toEmail, String orgName);
    void sendOrganizationRejected(String toEmail, String orgName, String reason);
    void sendOrganizationDocumentApproved(String toEmail, String orgName, String documentName);
    void sendOrganizationDocumentRejected(String toEmail, String orgName, String documentName, String reason);
    void sendEmployeeWelcomeWithCredentials(String toEmail, String fullName, String username, String tempPassword);
    void sendEmployeeDocumentApproved(String toEmail, String employeeName, String documentName, String orgName); // new [conversation_history:9]
    void sendEmployeeDocumentRejected(String toEmail, String employeeName, String documentName, String orgName, String reason); // new [conversation_history:9]
    void sendOrgDocReceived(String toEmail, String orgName, String documentName);
    void sendOrgDocPendingReview(String toEmail, String orgName, String documentName);
    void sendEmpDocReceived(String toEmail, String employeeName, String documentName, String orgName);
    void sendEmpDocPendingReview(String toEmail, String orgName, String employeeName, String employeeCode, String documentName);
    // Batch emails
    void sendBatchPendingReview(String toBankAdmin, String orgName, String salaryMonth, String totalAmount, Long batchId);
    void sendBatchApproved(String toOrgAdmin, String orgName, String salaryMonth, String totalAmount, Long batchId);
    void sendBatchRejected(String toOrgAdmin, String orgName, String salaryMonth, String reason, Long batchId);

    // Acknowledge to org when batch created
    void sendBatchAcknowledgedToOrg(String toOrgAdmin, String orgName, String salaryMonth, String totalAmount, Long batchId);
    void sendSalaryCreditedWithPayslipLink(String toEmail,
            String employeeName,
            String amount,
            String salaryMonth,
            String orgName,
            Long payslipId,
            String transactionRef);
}
