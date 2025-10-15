package com.aurionpro.service;

import com.aurionpro.filters.ConcernFilter;

public interface ConcernExportService {
    byte[] exportCsv(ConcernFilter filter, String sortBy, String sortDir);
    byte[] exportPdf(ConcernFilter filter, String sortBy, String sortDir);
}
