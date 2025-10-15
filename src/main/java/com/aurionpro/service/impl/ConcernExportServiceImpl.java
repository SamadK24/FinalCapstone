package com.aurionpro.service.impl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.aurionpro.entity.Concern;
import com.aurionpro.exceptions.BusinessRuleException;
import com.aurionpro.filters.ConcernFilter;
import com.aurionpro.repository.ConcernRepository;
import com.aurionpro.service.ConcernExportService;
import com.aurionpro.specs.ConcernSpecs;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConcernExportServiceImpl implements ConcernExportService {

    private static final int EXPORT_LIMIT = 10_000;
    private static final DateTimeFormatter TS = DateTimeFormatter.ISO_INSTANT;

    private final ConcernRepository concernRepository;

    @Override
    public byte[] exportCsv(ConcernFilter filter, String sortBy, String sortDir) {
        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(0, EXPORT_LIMIT, Sort.by(dir, sortBy));
        var page = concernRepository.findAll(ConcernSpecs.byFilter(filter), pageable);

        StringBuilder sb = new StringBuilder();
        sb.append('\ufeff'); // BOM for Excel
        sb.append("# GeneratedAt=").append(java.time.Instant.now())
          .append(", OrgId=").append(filter.getOrgId())
          .append(", Sort=").append(sortBy).append(" ").append(dir).append("\n");
        sb.append("id,status,category,employeeId,subject,updatedAt,createdAt\n");

        for (Concern c : page.getContent()) {
            sb.append(csv(c.getId()))
              .append(',').append(csv(c.getStatus() != null ? c.getStatus().name() : ""))
              .append(',').append(csv(c.getCategory() != null ? c.getCategory().name() : ""))
              .append(',').append(csv(c.getEmployeeId()))
              .append(',').append(csv(trim(c.getSubject(), 120)))
              .append(',').append(csv(c.getUpdatedAt() != null ? TS.format(c.getUpdatedAt()) : ""))
              .append(',').append(csv(c.getCreatedAt() != null ? TS.format(c.getCreatedAt()) : ""))
              .append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportPdf(ConcernFilter filter, String sortBy, String sortDir) {
        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(0, EXPORT_LIMIT, Sort.by(dir, sortBy));
        var page = concernRepository.findAll(ConcernSpecs.byFilter(filter), pageable);
        List<Concern> rows = page.getContent();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Paragraph title = new Paragraph("Concerns Export - Org " + filter.getOrgId(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph meta = new Paragraph("Generated: " + java.time.Instant.now() + " | Sort: " + sortBy + " " + dir,
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(10f);
            doc.add(meta);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.2f, 1.5f, 2f, 1.5f, 4f, 2.5f, 2.5f});

            addHeader(table, "ID");
            addHeader(table, "Status");
            addHeader(table, "Category");
            addHeader(table, "Employee ID");
            addHeader(table, "Subject");
            addHeader(table, "Updated At");
            addHeader(table, "Created At");

            for (Concern c : rows) {
                addCell(table, str(c.getId()));
                addCell(table, c.getStatus() != null ? c.getStatus().name() : "");
                addCell(table, c.getCategory() != null ? c.getCategory().name() : "");
                addCell(table, str(c.getEmployeeId()));
                addCell(table, trim(c.getSubject(), 120));
                addCell(table, c.getUpdatedAt() != null ? TS.format(c.getUpdatedAt()) : "");
                addCell(table, c.getCreatedAt() != null ? TS.format(c.getCreatedAt()) : "");
            }

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessRuleException("PDF export failed: " + e.getMessage());
        }
    }

    // Helper methods
    private static void addHeader(PdfPTable t, String s) {
        PdfPCell h = new PdfPCell(new Phrase(s, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        h.setHorizontalAlignment(Element.ALIGN_CENTER);
        h.setPadding(5f);
        t.addCell(h);
    }

    private static void addCell(PdfPTable t, String s) {
        PdfPCell c = new PdfPCell(new Phrase(s, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        c.setPadding(4f);
        t.addCell(c);
    }

    private static String csv(Object o) {
        String s = o == null ? "" : o.toString();
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }
}

