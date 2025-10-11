package com.aurionpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Override
    public void sendOrganizationRegistered(String toEmail, String orgName, String adminUsername) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Organization registration received: " + orgName);
        msg.setText(
            "Hello " + adminUsername + ",\n\n" +
            "Your organization \"" + orgName + "\" has been registered and is pending approval by Bank Admin.\n" +
            "Next steps:\n" +
            "1) Upload mandatory organization documents.\n" +
            "2) Wait for approval notification.\n" +
            "3) After approval, add employees and manage payroll/documents.\n\n" +
            "If this wasn’t initiated by you, please contact support.\n\n" +
            "Regards,\nMyProject"
        );
        mailSender.send(msg);
    }
    
    @Override
    public void sendOrganizationApproved(String toEmail, String orgName) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Organization approved: " + orgName);
        msg.setText("Good news!\n\nYour organization \"" + orgName + "\" has been approved by Bank Admin.\n" +
                "You can now add employees and proceed with payroll and document workflows.\n");
        mailSender.send(msg);
    }

    @Override
    public void sendOrganizationRejected(String toEmail, String orgName, String reason) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Organization request rejected: " + orgName);
        msg.setText("We’re sorry.\n\nYour organization \"" + orgName + "\" registration was rejected.\n" +
                "Reason: " + (reason == null ? "No reason provided" : reason) + "\n" +
                "Please review and resubmit the application with corrections.\n");
        mailSender.send(msg);
    }
    
    @Override
    public void sendOrganizationDocumentApproved(String toEmail, String orgName, String documentName) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Document approved: " + documentName);
        msg.setText(
            "Good news!\n\n" +
            "Your organization document \"" + documentName + "\" for \"" + orgName + "\" has been approved by Bank Admin.\n" +
            "It is now available for downstream use.\n\n" +
            "Regards,\nMyProject"
        );
        mailSender.send(msg);
    }

    @Override
    public void sendOrganizationDocumentRejected(String toEmail, String orgName, String documentName, String reason) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Document rejected: " + documentName);
        msg.setText(
            "We’re sorry.\n\n" +
            "Your organization document \"" + documentName + "\" for \"" + orgName + "\" was rejected by Bank Admin.\n" +
            "Reason: " + (reason == null ? "No reason provided" : reason) + "\n\n" +
            "Please review the comments and re-upload the corrected document.\n\n" +
            "Regards,\nMyProject"
        );
        mailSender.send(msg);
    }
    
    @Override
    public void sendEmployeeWelcomeWithCredentials(String toEmail, String fullName, String username, String tempPassword) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Welcome to MyProject — Your account details");
        msg.setText(
            "Hello " + (fullName == null ? username : fullName) + ",\n\n" +
            "An account has been created for you.\n\n" +
            "Username: " + username + "\n" +
            "Temporary password: " + tempPassword + "\n\n" +
            "For security, please sign in and change your password immediately.\n\n" +
            "Regards,\nMyProject"
        );
        mailSender.send(msg);
    }
    
    @Override
    public void sendEmployeeDocumentApproved(String toEmail, String employeeName, String documentName, String orgName) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Document approved: " + documentName);
        msg.setText(
            "Hello " + (employeeName == null ? "" : employeeName) + ",\n\n" +
            "Your document \"" + documentName + "\" for organization \"" + orgName + "\" has been approved.\n" +
            "No further action is required.\n\n" +
            "Regards,\nMyProject"
        );
        mailSender.send(msg);
    }

    @Override
    public void sendEmployeeDocumentRejected(String toEmail, String employeeName, String documentName, String orgName, String reason) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Document rejected: " + documentName);
        msg.setText(
            "Hello " + (employeeName == null ? "" : employeeName) + ",\n\n" +
            "Your document \"" + documentName + "\" for organization \"" + orgName + "\" was rejected.\n" +
            "Reason: " + (reason == null ? "No reason provided" : reason) + "\n\n" +
            "Please correct and re-upload the document.\n\n" +
            "Regards,\nMyProject"
        );
        mailSender.send(msg);
    }
    
    @Override
    public void sendOrgDocReceived(String toEmail, String orgName, String documentName) {
        SimpleMailMessage m = base();
        m.setTo(toEmail);
        m.setSubject("Received: " + documentName);
        m.setText("Your document \"" + documentName + "\" for \"" + orgName + "\" was received and is pending Bank Admin review.\n\nRegards,\nMyProject");
        mailSender.send(m);
    }

    @Override
    public void sendOrgDocPendingReview(String toEmail, String orgName, String documentName) {
        SimpleMailMessage m = base();
        m.setTo(toEmail);
        m.setSubject("Review pending: " + documentName);
        m.setText("A new organization document \"" + documentName + "\" from \"" + orgName + "\" is awaiting review.\n\nRegards,\nMyProject");
        mailSender.send(m);
    }

    @Override
    public void sendEmpDocReceived(String toEmail, String employeeName, String documentName, String orgName) {
        SimpleMailMessage m = base();
        m.setTo(toEmail);
        m.setSubject("Received: " + documentName);
        m.setText("Hello " + safe(employeeName) + ",\n\nYour document \"" + documentName + "\" for \"" + orgName + "\" was received and is pending organization review.\n\nRegards,\nMyProject");
        mailSender.send(m);
    }

    @Override
    public void sendEmpDocPendingReview(String toEmail, String orgName, String employeeName, String employeeCode, String documentName) {
        SimpleMailMessage m = base();
        m.setTo(toEmail);
        m.setSubject("Employee document pending: " + documentName);
        m.setText("A new employee document \"" + documentName + "\" is awaiting review.\nEmployee: " + safe(employeeName) + " (" + safe(employeeCode) + ")\nOrganization: " + orgName + "\n\nRegards,\nMyProject");
        mailSender.send(m);
    }
    
 // Inside SmtpEmailService

    @Override
    public void sendBatchPendingReview(String toBankAdmin, String orgName, String salaryMonth, String totalAmount, Long batchId) {
        var m = base();
        m.setTo(toBankAdmin);
        m.setSubject("Payroll batch pending review: " + orgName + " (" + salaryMonth + ")");
        m.setText("A new payroll batch is awaiting approval.\nOrg: " + orgName +
                "\nMonth: " + salaryMonth + "\nTotal: " + totalAmount + "\nBatch ID: " + batchId + "\n\nRegards,\nMyProject");
        mailSender.send(m);
    }

    @Override
    public void sendBatchApproved(String toOrgAdmin, String orgName, String salaryMonth, String totalAmount, Long batchId) {
        var m = base();
        m.setTo(toOrgAdmin);
        m.setSubject("Payroll batch approved: " + salaryMonth);
        m.setText("Your payroll batch has been approved and funded.\nOrg: " + orgName +
                "\nMonth: " + salaryMonth + "\nTotal: " + totalAmount + "\nBatch ID: " + batchId + "\n\nRegards,\nMyProject");
        mailSender.send(m);
    }

    @Override
    public void sendBatchRejected(String toOrgAdmin, String orgName, String salaryMonth, String reason, Long batchId) {
        var m = base();
        m.setTo(toOrgAdmin);
        m.setSubject("Payroll batch rejected: " + salaryMonth);
        m.setText("Your payroll batch was rejected.\nOrg: " + orgName +
                "\nMonth: " + salaryMonth + "\nBatch ID: " + batchId + "\nReason: " + (reason == null ? "No reason provided" : reason)
                + "\n\nRegards,\nMyProject");
        mailSender.send(m);
    }

    @Override
    public void sendBatchAcknowledgedToOrg(String toOrgAdmin, String orgName, String salaryMonth, String totalAmount, Long batchId) {
        var m = base();
        m.setTo(toOrgAdmin);
        m.setSubject("Payroll batch received: " + salaryMonth);
        m.setText("Your payroll batch was created and queued for bank approval.\nOrg: " + orgName +
                "\nMonth: " + salaryMonth + "\nTotal: " + totalAmount + "\nBatch ID: " + batchId + "\n\nRegards,\nMyProject");
        mailSender.send(m);
    }
    
    @Override
    public void sendSalaryCreditedWithPayslipLink(String toEmail,
                                                  String employeeName,
                                                  String amount,
                                                  String salaryMonth,
                                                  String orgName,
                                                  Long payslipId,
                                                  String transactionRef) {
        var m = base(); // your helper that sets From, etc.
        m.setTo(toEmail);
        m.setSubject("Salary credited for " + salaryMonth);
        String body = "Hi " + employeeName + ",\n\n" +
                "Your salary has been credited.\n" +
                "Organization: " + orgName + "\n" +
                "Amount: " + amount + "\n" +
                "Payslip ID: " + payslipId + "\n" +
                "Transaction Ref: " + transactionRef + "\n\n" +
                "You can log in to view/download your payslip.\n\n" +
                "Regards,\nMyProject";
        m.setText(body);
        mailSender.send(m);
    }


    
    // helpers
    private SimpleMailMessage base() {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(fromAddress);
        return m;
    }
    private String safe(String s) { return s == null ? "" : s; }

}

