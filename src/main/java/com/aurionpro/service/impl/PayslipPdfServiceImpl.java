package com.aurionpro.service.impl;

import com.aurionpro.dtos.EmployeePayslipView;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.service.PayslipPdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PayslipPdfServiceImpl implements PayslipPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public byte[] render(EmployeePayslipView v) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Header
            Paragraph title = new Paragraph("Payslip - " + v.getPeriodLabel(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph orgP = new Paragraph(
                    v.getOrganization().getName()
                            + (nullable(v.getOrganization().getAddress()) ? "" : "\n" + v.getOrganization().getAddress())
                            + (nullable(v.getOrganization().getContactNumber()) ? "" : "\nContact: " + v.getOrganization().getContactNumber()),
                    FontFactory.getFont(FontFactory.HELVETICA, 10)
            );
            orgP.setAlignment(Element.ALIGN_CENTER);
            orgP.setSpacingAfter(12f);
            doc.add(orgP);

            // Employee + Period block
            PdfPTable empTable = new PdfPTable(2);
            empTable.setWidths(new float[]{1f, 2f});
            empTable.setWidthPercentage(100f);
            empTable.setSpacingAfter(10f);

            addLabelValue(empTable, "Employee Code", v.getEmployee().getCode());
            addLabelValue(empTable, "Employee Name", v.getEmployee().getName());
            addLabelValue(empTable, "Department", nvl(v.getEmployee().getDepartment()));
            addLabelValue(empTable, "Designation", nvl(v.getEmployee().getDesignation()));
            addLabelValue(empTable, "Period Start", v.getPeriodStart().format(DATE_FMT));
            addLabelValue(empTable, "Period End", v.getPeriodEnd().format(DATE_FMT));
            doc.add(empTable);

            // Earnings and Deductions
            PdfPTable compTable = new PdfPTable(2);
            compTable.setWidths(new float[]{1f, 1f});
            compTable.setWidthPercentage(100f);
            compTable.setSpacingBefore(5f);
            compTable.setSpacingAfter(10f);

            PdfPTable earnTable = buildComponentsTable("Earnings", v.getEarnings());
            PdfPTable dedTable  = buildComponentsTable("Deductions", v.getDeductions());

            PdfPCell left = new PdfPCell(earnTable); left.setPadding(5f); left.setBorderWidth(0.5f);
            PdfPCell right = new PdfPCell(dedTable); right.setPadding(5f); right.setBorderWidth(0.5f);

            compTable.addCell(left);
            compTable.addCell(right);
            doc.add(compTable);

            // Totals
            PdfPTable totals = new PdfPTable(2);
            totals.setWidths(new float[]{2f, 1f});
            totals.setWidthPercentage(60f);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addTotalRow(totals, "Gross Earnings", v.getTotals().getGrossEarnings(), v.getTotals().getCurrency());
            addTotalRow(totals, "Total Deductions", v.getTotals().getTotalDeductions(), v.getTotals().getCurrency());
            addTotalRowBold(totals, "Net Pay", v.getTotals().getNetPay(), v.getTotals().getCurrency());
            totals.setSpacingAfter(10f);
            doc.add(totals);

            // Payment details
            PdfPTable pay = new PdfPTable(2);
            pay.setWidths(new float[]{1f, 2f});
            pay.setWidthPercentage(100f);

            addLabelValue(pay, "Paid Date", v.getPayment().getPaidDate().format(DATE_FMT));
            addLabelValue(pay, "Payment Ref", nvl(v.getPayment().getPaymentRef()));
            addLabelValue(pay, "Bank", nvl(v.getPayment().getBankName()));
            addLabelValue(pay, "Account", nvl(v.getPayment().getMaskedAccount()));
            pay.setSpacingAfter(10f);
            doc.add(pay);

            // Footer
            Paragraph gen = new Paragraph("Generated at: " + v.getGeneratedAt().toString(),
                    FontFactory.getFont(FontFactory.HELVETICA, 8));
            gen.setSpacingBefore(8f);
            doc.add(gen);

            Paragraph disc = new Paragraph(nvl(v.getDisclaimer()),
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8));
            doc.add(disc);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessRuleException("PDF generation failed: " + e.getMessage());
        }
    }

    private PdfPTable buildComponentsTable(String title, List<EmployeePayslipView.Component> comps) {
        PdfPTable t = new PdfPTable(2);
        try { t.setWidths(new float[]{2f, 1f}); } catch (Exception ignored) {}
        t.setWidthPercentage(100f);

        PdfPCell h = new PdfPCell(new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        h.setColspan(2); h.setPadding(5f);
        t.addCell(h);

        if (comps != null) {
            for (var c : comps) {
                t.addCell(cell(c.getName()));
                t.addCell(cellRight(amount(c.getAmount())));
            }
        }
        return t;
    }

    private void addLabelValue(PdfPTable t, String label, String value) {
        t.addCell(cell(label));
        t.addCell(cell(value));
    }

    private void addTotalRow(PdfPTable t, String label, BigDecimal amt, String cur) {
        t.addCell(cellRight(label));
        t.addCell(cellRight(amountWithCur(amt, cur)));
    }

    private void addTotalRowBold(PdfPTable t, String label, BigDecimal amt, String cur) {
        PdfPCell l = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        l.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(l);
        PdfPCell r = new PdfPCell(new Phrase(amountWithCur(amt, cur), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        r.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(r);
    }

    private PdfPCell cell(String s) {
        PdfPCell c = new PdfPCell(new Phrase(nvl(s)));
        c.setPadding(5f);
        return c;
    }

    private PdfPCell cellRight(String s) {
        PdfPCell c = new PdfPCell(new Phrase(nvl(s)));
        c.setPadding(5f);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private String amount(BigDecimal a) { return a == null ? "0" : a.stripTrailingZeros().toPlainString(); }
    private String amountWithCur(BigDecimal a, String cur) { return (cur == null ? "" : cur + " ") + amount(a); }
    private boolean nullable(String s) { return s == null || s.isBlank(); }
    private String nvl(String s) { return s == null ? "" : s; }
}

